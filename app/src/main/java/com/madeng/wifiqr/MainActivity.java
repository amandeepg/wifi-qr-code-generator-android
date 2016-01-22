package com.madeng.wifiqr;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.analytics.HitBuilders;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.AdapterViewItemClickEvent;
import com.jakewharton.rxbinding.widget.RxAutoCompleteTextView;
import com.madeng.wifiqr.utils.AnimationUtils;
import com.madeng.wifiqr.utils.RootUtilsFactory;
import com.madeng.wifiqr.utils.ViewUtils;
import com.madeng.wifiqr.utils.WifiUtilsFactory;
import com.madeng.wifiqr.utils.rx.RxBus;
import com.madeng.wifiqr.utils.rx.RxUtils;
import com.melnykov.fab.FloatingActionButton;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tumblr.backboard.performer.Performer;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.UtilityFunctions;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int SPINNER_FADE_DURATION = 300;
    public static final String STATE_KEY_QR_SHOWN = "STATE_KEY_QR_SHOWN";

    @Bind(R.id.name_field) AutoCompleteTextView nameField;
    @Bind(R.id.pwd_field) EditText password;
    @Bind(R.id.auth_spinner) MaterialBetterSpinner authSpinner;
    @Bind(R.id.generate_button) FloatingActionButton generateButton;
    @Bind(R.id.main_qr_view) QrView mainQrView;
    @Bind(R.id.fab_spinner) CircularProgressView fabSpinner;

    private static final List<QrNetworkInfo> sScannedNetworks = new ArrayList<>();
    private static final List<QrNetworkInfo> sRememberedNetworks = new ArrayList<>();

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private SpringSystem mSpringSystem;
    private NetworkInfoDataService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final String[] authArray = getResources().getStringArray(R.array.security_types);
        authSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                authArray));
        authSpinner.setText(authArray[0]);

        generateButton.setImageDrawable(MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.QRCODE)
                .setColor(Color.WHITE)
                .build());

        mSpringSystem = SpringSystem.create();
        dataService = new NetworkInfoDataService(this);

        initPubSub();

        final Observable<QrNetworkInfo> loadNetworksObservable = RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .observeOn(Schedulers.io())
                .doOnNext(this::sendAnalyticsCoarseLocationPermission)
                .doOnNext(granted -> {
                    if (granted && sScannedNetworks.isEmpty()) {
                        sScannedNetworks.addAll(WifiUtilsFactory.getInstance().getScannedNetworks(this));
                    }
                    if (sRememberedNetworks.isEmpty()) {
                        sRememberedNetworks.addAll(WifiUtilsFactory.getInstance().getRememberedNetworks(this));
                    }
                })
                .map(UtilityFunctions.returnNull())
                .flatMap(nothing -> RootUtilsFactory.getInstance().tryRootNetworks(this))
                .doOnNext(dataService::save);

        if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_KEY_QR_SHOWN)) {
            mCompositeSubscription.add(
                    loadNetworksObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnCompleted(this::setNameAdapter)
                            .subscribe());
            showQrView(false);
        } else {
            mCompositeSubscription.add(
                    loadNetworksObservable
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnCompleted(this::setNameAdapter)
                            .doOnCompleted(this::prefillConnectedNetwork)
                            .subscribe());
        }

        App.getTracker(this).setScreenName("Main");
        if (savedInstanceState == null) {
            App.getTracker(this).send(new HitBuilders.ScreenViewBuilder().build());
            App.getAnswers(this).logContentView(new ContentViewEvent().putContentName("MainActivity"));
        }
    }

    @Override
    protected void onDestroy() {
        mCompositeSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_KEY_QR_SHOWN, mainQrView.getVisibility() == View.VISIBLE);
    }

    private void prefillConnectedNetwork() {
        if (getNameText().isEmpty()) {
            RxBus.send(new FillFieldsTask(WifiUtilsFactory.getInstance().getConnectedSsid(this)));
        }
    }

    private void initPubSub() {
        final Observable<Void> generateTap = RxView.clicks(generateButton).cache();

        // tap on 'generate' -> -> show error if necessary
        mCompositeSubscription.add(
                generateTap
                        .observeOn(Schedulers.computation())
                        .map(e -> getNetworkInfoFromDisplayedUi())
                        .map(MainActivity::getFieldValidationError)
                        .filter(resId -> resId > 0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::showError));

        // tap on 'generate', if valid -> GenerateQrTask
        mCompositeSubscription.add(
                generateTap
                        .observeOn(Schedulers.computation())
                        .map(e -> getNetworkInfoFromDisplayedUi())
                        .doOnNext(e -> sendAnalyticsGenerateTap(false))
                        .filter(this::isFieldsValid)
                        .doOnNext(e -> sendAnalyticsGenerateTap(true))
                        .subscribe(info -> RxBus.send(new GenerateQrTask(info))));

        // tap an autocomplete row -> FillFieldsTask
        mCompositeSubscription.add(
                RxAutoCompleteTextView.itemClickEvents(nameField)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(e -> sendAnalyticsAutoCompleteTap())
                        .map(MainActivity::getNameFromAutoCompleteClickEvent)
                        .subscribe(name -> RxBus.send(new FillFieldsTask(name))));

        mCompositeSubscription.add(
                RxBus.toObservable(FillFieldsTask.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(e -> e.name)
                        .filter(RxUtils.isNotNull())
                        .doOnNext(nameField::setText)
                        .doOnNext(e -> nameField.dismissDropDown())
                        .flatMap(dataService::networkInfo)
                        .filter(RxUtils.isNotNull())
                        .doOnNext(info -> password.setText(info.getPassword()))
                        .doOnNext(info -> authSpinner.setText(QrNetworkInfo.getAuthStringResRepresentation(info)))
                        .subscribe(info -> RxBus.send(new GenerateQrTask(info))));

        mCompositeSubscription.add(
                RxBus.toObservable(SaveNetworkTask.class)
                        .map(e -> e.info)
                        .doOnNext(e -> sendAnalyticsSave())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(e -> Toast.makeText(this, R.string.saved_success, Toast.LENGTH_SHORT).show())
                        .subscribe(dataService::save));

        mCompositeSubscription.add(
                RxBus.toObservable(SaveNetworkTask.class)
                        .throttleLast(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(UtilityFunctions.returnNull())
                        .doOnNext(nothing -> setNameAdapter())
                        .subscribe());

        mCompositeSubscription.add(
                RxBus.toObservable(ShareNetworkTask.class)
                        .doOnNext(e -> sendAnalyticsShare())
                        .map(e -> e.info)
                        .doOnNext(dataService::save)
                        .map(QrNetworkInfo::getName)
                        .subscribe(name -> Navigator.shareQrCode(this, name)));

        mCompositeSubscription.add(
                RxBus.toObservable(GenerateQrTask.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(e -> e.info)
                        .filter(this::isFieldsValid)
                        .doOnNext(e -> ViewUtils.hideKeyboard(this))
                        .doOnNext(e -> showSpinner())
                        .doOnNext(e -> hideQrView())
                        .flatMap(mainQrView::showQrCode)
                        .doOnNext(e -> hideSpinner())
                        .doOnNext(e -> showQrView(true))
                        .subscribe());
    }

    private void sendAnalyticsCoarseLocationPermission(boolean granted) {
        Timber.d("ACCESS_COARSE_LOCATION = %s", granted);
        final String grantedStr = granted ? "Granted" : "Denied";
        App.getAnswers(this).logCustom(new CustomEvent("Access Coarse Location")
                .putCustomAttribute("Given", grantedStr));
        sendGaEvent("Permission", "Access Coarse Location", grantedStr);
    }

    private void sendAnalyticsShare() {
        sendGaEvent("Tap", "Share");
        App.getAnswers(this).logCustom(new CustomEvent("Tap Share"));
    }

    private void sendAnalyticsSave() {
        sendGaEvent("Tap", "Save");
        App.getAnswers(this).logCustom(new CustomEvent("Tap Save"));
    }

    private void sendAnalyticsAutoCompleteTap() {
        sendGaEvent("Tap", "AutoCompleteSsid");
        App.getAnswers(this).logCustom(new CustomEvent("Tap AutoCompleteSsid"));
    }

    private void sendAnalyticsGenerateTap(boolean attempt) {
        App.getAnswers(this).logCustom(new CustomEvent("Tap Generate " + (attempt ? "Attempt" : "Success")));
        sendGaEvent("Tap", "Generate", attempt ? "Attempt" : "Success");
    }

    private void sendGaEvent(@NotNull final String category,
                             @NotNull final String action) {
        App.getTracker(this).send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    private void sendGaEvent(@NotNull final String category,
                             @NotNull final String action,
                             @NotNull final String label) {
        App.getTracker(this).send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    private void showQrView(boolean animate) {
        mainQrView.setVisibility(View.VISIBLE);

        if (animate) {
            ViewUtils.setPivotForACenteredOnB(mainQrView, generateButton);

            mSpringSystem.createSpring()
                    .setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(45, 6.5))
                    .addListener(new Performer(mainQrView, View.SCALE_X))
                    .addListener(new Performer(mainQrView, View.SCALE_Y))
                    .setEndValue(1);
        }
    }

    private void hideQrView() {
        mainQrView.setVisibility(View.INVISIBLE);
    }

    private void showSpinner() {
        fabSpinner.setVisibility(View.VISIBLE);
        final ObjectAnimator progressFadeIn = ObjectAnimator.ofFloat(fabSpinner, "alpha", 0, 1);
        progressFadeIn.setDuration(SPINNER_FADE_DURATION);
        progressFadeIn.start();
    }

    private void hideSpinner() {
        final float currentAlpha = fabSpinner.getAlpha();
        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(fabSpinner, "alpha", currentAlpha, 0);
        fadeOut.addListener(new AnimationUtils.EndListenerAdapter(() -> fabSpinner.setVisibility(View.INVISIBLE)));
        fadeOut.setDuration((int) (SPINNER_FADE_DURATION * currentAlpha));
        fadeOut.start();
    }

    @NotNull
    private String getNameText() {
        return nameField.getText().toString();
    }

    @NotNull
    private String getPasswordText() {
        return password.getText().toString();
    }

    private static String getNameFromAutoCompleteClickEvent(AdapterViewItemClickEvent e) {
        return ((QrNetworkInfo) (e.view().getAdapter().getItem(e.position()))).getName();
    }

    private void setNameAdapter() {
        mCompositeSubscription.add(
                dataService.networkInfos()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(Schedulers.computation())
                        .flatMapIterable(UtilityFunctions.identity())
                        .mergeWith(Observable.from(sScannedNetworks))
                        .mergeWith(Observable.from(sRememberedNetworks))
                        .toList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(list -> QrNetworkInfo.removeDuplicates(list, QrNetworkInfo::getName))
                        .subscribe(this::setNameAdapter));
    }

    private void setNameAdapter(@NotNull final List<QrNetworkInfo> infos) {
        nameField.setAdapter(new NetworkInfoAutoCompleteAdapter(this, infos));
    }

    @NotNull
    private QrNetworkInfo getNetworkInfoFromDisplayedUi() {
        return new QrNetworkInfo(
                getNameText(),
                getPasswordText(),
                QrNetworkInfo.getAuthFromStringResRepresentation(authSpinner.getText().toString(), getResources()),
                QrNetworkInfo.SOURCE_SAVED
        );
    }

    private void showError(@StringRes final int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private boolean isFieldsValid(@NotNull QrNetworkInfo info) {
        return getFieldValidationError(info) == 0;
    }

    private static int getFieldValidationError(@NotNull final QrNetworkInfo data) {
        // Ensure SSID is longer than zero characters
        if (data.getName().length() == 0) {
            return R.string.no_name;
        }

        if (data.getAuth() == QrNetworkInfo.AUTH_WPA || data.getAuth() == QrNetworkInfo.AUTH_WEP) {
            if (data.getPassword().length() == 0) {
                return R.string.no_pass;
            }
        }

        return 0;
    }
}
