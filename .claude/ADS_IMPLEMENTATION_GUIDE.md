# Google AdMob в Compose Multiplatform (Android + iOS)

Полная инструкция по интеграции Google AdMob (Banner + Interstitial) в Kotlin Multiplatform проект с Compose.

---

## Содержание

1. [Архитектура](#архитектура)
2. [Зависимости](#зависимости)
3. [Common (expect-интерфейсы)](#common-expect-интерфейсы)
4. [Android реализация](#android-реализация)
5. [iOS реализация](#ios-реализация)
6. [Desktop/Web заглушки](#desktopweb-заглушки)
7. [Интеграция в App](#интеграция-в-app)
8. [Использование на экранах](#использование-на-экранах)
9. [Чеклист перед релизом](#чеклист-перед-релизом)

---

## Архитектура

```
commonMain/ads/
├── BannerAdView.kt           # expect Composable для баннера
└── InterstitialAdManager.kt  # expect class для интерстишл

androidMain/ads/
├── BannerAdView.android.kt          # actual — AndroidView + AdView
├── InterstitialAdManager.android.kt  # actual — Google InterstitialAd
└── ActivityProvider.kt               # WeakReference на Activity

iosMain/ads/
├── BannerAdView.ios.kt              # actual — UIKitView + GADBannerView
├── InterstitialAdManager.ios.kt      # actual — GADInterstitialAd
├── ViewControllerProvider.kt         # Хранение UIViewController
└── ATTManager.kt                     # App Tracking Transparency

jvmMain/ads/   # заглушки (no-op)
webMain/ads/   # заглушки (no-op)
```

**Принцип**: `expect/actual` — общий интерфейс в `commonMain`, платформенные реализации в `androidMain` и `iosMain`.

---

## Зависимости

### gradle/libs.versions.toml

```toml
[versions]
admob = "23.6.0"

[libraries]
admob = { module = "com.google.android.gms:play-services-ads", version.ref = "admob" }
```

### composeApp/build.gradle.kts

**Android зависимость:**

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.admob)
        }
    }
}
```

**iOS зависимость (CocoaPods):**

```kotlin
kotlin {
    cocoapods {
        // ... базовая конфигурация ...

        pod("Google-Mobile-Ads-SDK") {
            version = "12.0.0"
            moduleName = "GoogleMobileAds"
            headers = "GoogleMobileAds/GoogleMobileAds.h"
        }
    }
}
```

### iosApp/Podfile

```ruby
target 'iosApp' do
  use_frameworks!
  platform :ios, '14.0'
  pod 'composeApp', :path => '../composeApp'
end
```

После изменений запустить:

```bash
cd iosApp && pod install
```

---

## Common (expect-интерфейсы)

### commonMain/.../ads/BannerAdView.kt

```kotlin
package com.yourpackage.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BannerAdView(
    modifier: Modifier = Modifier
)
```

### commonMain/.../ads/InterstitialAdManager.kt

```kotlin
package com.yourpackage.ads

expect class InterstitialAdManager {
    fun loadAd()
    fun showAd(onAdDismissed: () -> Unit)
    fun isAdLoaded(): Boolean
}

expect fun createInterstitialAdManager(): InterstitialAdManager
```

---

## Android реализация

### 1. AndroidManifest.xml

Добавить внутрь `<application>`:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
```

Permissions (если отсутствуют):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 2. ActivityProvider.kt

Необходим для передачи `Activity` в SDK рекламы:

```kotlin
package com.yourpackage.ads

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? = activityRef?.get()
}
```

### 3. MainActivity.kt — инициализация SDK

```kotlin
import com.yourpackage.ads.ActivityProvider
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация AdMob SDK
        MobileAds.initialize(this)

        // Сохраняем ссылку на Activity для показа рекламы
        ActivityProvider.setActivity(this)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.setActivity(this)
    }
}
```

### 4. BannerAdView.android.kt

```kotlin
package com.yourpackage.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
actual fun BannerAdView(modifier: Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // ЗАМЕНИТЬ на свой Ad Unit ID
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
```

### 5. InterstitialAdManager.android.kt

```kotlin
package com.yourpackage.ads

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

actual class InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    // ЗАМЕНИТЬ на свой Ad Unit ID
    private val adUnitId = "ca-app-pub-3940256099942544/1033173712"

    actual fun loadAd() {
        if (isLoading || interstitialAd != null) return

        val activity = ActivityProvider.getActivity() ?: run {
            Log.e("InterstitialAdManager", "Activity is null, cannot load ad")
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("InterstitialAdManager", "Ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialAdManager", "Ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                }
            }
        )
    }

    actual fun showAd(onAdDismissed: () -> Unit) {
        val activity = ActivityProvider.getActivity()
        val ad = interstitialAd

        if (activity == null || ad == null) {
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadAd() // Предзагрузка следующей
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadAd()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitialAdManager", "Ad showed")
            }
        }

        ad.show(activity)
    }

    actual fun isAdLoaded(): Boolean = interstitialAd != null
}

actual fun createInterstitialAdManager(): InterstitialAdManager = InterstitialAdManager()
```

---

## iOS реализация

### 1. Info.plist

Добавить в `iosApp/iosApp/Info.plist`:

```xml
<!-- AdMob App ID -->
<key>GADApplicationIdentifier</key>
<string>ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX</string>

<!-- App Tracking Transparency (обязательно для iOS 14+) -->
<key>NSUserTrackingUsageDescription</key>
<string>This identifier will be used to deliver personalized ads to you.</string>
```

#### SKAdNetwork IDs (ОБЯЗАТЕЛЬНО для прода)

Без `SKAdNetworkItems` AdMob в проде будет отдавать пустые ответы (no fill) на большинство запросов, потому что рекламные сети не смогут атрибутировать установки.

Добавить в `Info.plist` внутрь корневого `<dict>`:

```xml
<key>SKAdNetworkItems</key>
<array>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>cstr6suwn9.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>4fzdc2evr5.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>2fnua5tdw4.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>ydx93a7ass.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>p78axxw29g.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>v72qych5uu.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>ludvb6z3bs.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>cp8zw746q7.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>3sh42y64q3.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>c6k4g5qg8m.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>s39g8k73mm.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>wg4vff78zm.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>3qy4746246.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>f38h382jlk.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>hs6bdukanm.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>mlmmfzh3r3.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>v4nxqhlyqp.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>wzmmz9fp6w.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>su67r6k2v3.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>yclnxrl5pm.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>t38b2kh725.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>7ug5zh24hu.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>gta9lk7p23.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>vutu7akeur.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>y5ghdn5j9k.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>v9wttpbfk9.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>n38lu8286q.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>47vhws6wlr.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>kbd757ywx3.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>9t245vhmpl.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>a2p9lx4jpn.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>22mmun2rn5.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>44jx6755aq.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>k674qkevps.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>4468km3ulz.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>2u9pt9hc89.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>8s468mfl3y.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>klf5c3l5u5.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>ppxm28t8ap.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>kbmxgpxpgc.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>uw77j35x4d.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>578prtvx9j.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>4dzt52r2t5.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>tl55sbb4fm.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>c3frkrj4fj.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>e5fvkxwrpn.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>8c4e2ghe7u.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>3rd42ekr43.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>97r2b46745.skadnetwork</string>
    </dict>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>3qcr597p9d.skadnetwork</string>
    </dict>
</array>
```

> Полный актуальный список: https://developers.google.com/admob/ios/3p-skadnetworks

### 2. ViewControllerProvider.kt

Нужен для передачи `UIViewController` в рекламный SDK.

> **ВАЖНО**: `UIApplication.sharedApplication.keyWindow` deprecated с iOS 13 и может вернуть `nil` на iOS 15+ с multi-scene приложениями. Используем `connectedScenes` API.

```kotlin
package com.yourpackage.ads

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.UISceneActivationStateForegroundActive
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
object ViewControllerProvider {
    private var viewControllerRef: UIViewController? = null

    fun setViewController(viewController: UIViewController) {
        viewControllerRef = viewController
    }

    fun getViewController(): UIViewController? {
        return viewControllerRef ?: getKeyWindowRootViewController()
    }

    private fun getKeyWindowRootViewController(): UIViewController? {
        val scenes = UIApplication.sharedApplication.connectedScenes
        for (scene in scenes) {
            val windowScene = scene as? UIWindowScene ?: continue
            if (windowScene.activationState == UISceneActivationStateForegroundActive) {
                val windows = windowScene.windows
                for (window in windows) {
                    val uiWindow = window as? platform.UIKit.UIWindow ?: continue
                    if (uiWindow.isKeyWindow()) {
                        return uiWindow.rootViewController
                    }
                }
            }
        }
        return null
    }
}
```

### 3. ATTManager.kt — App Tracking Transparency

Запрос разрешения на трекинг (обязательно с iOS 14):

```kotlin
package com.yourpackage.ads

import platform.Foundation.NSLog
import platform.AppTrackingTransparency.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
object ATTManager {
    fun requestTrackingAuthorization(completion: () -> Unit) {
        val status = ATTrackingManager.trackingAuthorizationStatus()

        if (status == ATTrackingManagerAuthorizationStatusNotDetermined) {
            ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { newStatus ->
                NSLog("ATT Status: $newStatus")
                completion()
            }
        } else {
            NSLog("ATT already determined: $status")
            completion()
        }
    }
}
```

### 4. MainViewController.kt — инициализация SDK

```kotlin
package com.yourpackage

import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController
import cocoapods.Google_Mobile_Ads_SDK.GADMobileAds
import com.yourpackage.ads.ViewControllerProvider
import com.yourpackage.ads.ATTManager
import platform.Foundation.NSLog
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.UIKit.UIApplication

private var isSdkInitialized = mutableStateOf(false)

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    // 1. Запросить ATT разрешение
    // 2. Инициализировать Google Mobile Ads SDK
    ATTManager.requestTrackingAuthorization {
        GADMobileAds.sharedInstance().startWithCompletionHandler { status ->
            NSLog("Google Mobile Ads SDK initialized (iOS)")
            isSdkInitialized.value = true
        }
    }

    return ComposeUIViewController {
        LaunchedEffect(Unit) {
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (rootVC != null) {
                ViewControllerProvider.setViewController(rootVC)
            }
        }

        val sdkReady by isSdkInitialized

        // isSdkReady передается в App чтобы загрузка рекламы
        // началась только после инициализации SDK
        App(isSdkReady = sdkReady)
    }
}
```

### 5. BannerAdView.ios.kt

```kotlin
package com.yourpackage.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import cocoapods.Google_Mobile_Ads_SDK.GADBannerView
import cocoapods.Google_Mobile_Ads_SDK.GADAdSizeBanner
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAdView(modifier: Modifier) {
    UIKitView(
        modifier = modifier.fillMaxWidth().height(50.dp),
        factory = {
            val bannerView = GADBannerView(CGRectZero.readValue())
            // ЗАМЕНИТЬ на свой Ad Unit ID
            bannerView.adUnitID = "ca-app-pub-3940256099942544/2934735716"

            val viewController = ViewControllerProvider.getViewController()
            if (viewController != null) {
                bannerView.rootViewController = viewController
                bannerView.setAdSize(GADAdSizeBanner.readValue())
                bannerView.loadRequest(GADRequest())
            }

            bannerView
        }
    )
}
```

### 6. InterstitialAdManager.ios.kt

```kotlin
package com.yourpackage.ads

import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenContentDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenPresentingAdProtocol
import platform.Foundation.NSLog
import cocoapods.Google_Mobile_Ads_SDK.GADInterstitialAd
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class InterstitialAdManager {
    private var interstitialAd: GADInterstitialAd? = null
    private var isLoading = false

    // Делегат хранится как свойство чтобы не был собран GC
    private var currentDelegate: GADFullScreenContentDelegateProtocol? = null

    // ЗАМЕНИТЬ на свой Ad Unit ID
    private val adUnitId = "ca-app-pub-3940256099942544/4411468910"

    actual fun loadAd() {
        if (isLoading || interstitialAd != null) return

        isLoading = true
        val request = GADRequest()

        GADInterstitialAd.loadWithAdUnitID(
            adUnitID = adUnitId,
            request = request,
            completionHandler = { ad, error ->
                isLoading = false

                if (error != null) {
                    NSLog("InterstitialAdManager: Failed to load - ${error.localizedDescription}")
                    interstitialAd = null
                } else if (ad != null) {
                    NSLog("InterstitialAdManager: Ad loaded successfully")
                    interstitialAd = ad
                }
            }
        )
    }

    actual fun showAd(onAdDismissed: () -> Unit) {
        val viewController = ViewControllerProvider.getViewController()
        val ad = interstitialAd

        if (viewController == null || ad == null) {
            onAdDismissed()
            return
        }

        // ВАЖНО: делегат наследует NSObject() и реализует протокол
        currentDelegate = object : NSObject(), GADFullScreenContentDelegateProtocol {
            override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
                interstitialAd = null
                currentDelegate = null
                loadAd() // Предзагрузка следующей
                onAdDismissed()
            }

            override fun ad(
                ad: GADFullScreenPresentingAdProtocol,
                didFailToPresentFullScreenContentWithError: platform.Foundation.NSError
            ) {
                interstitialAd = null
                currentDelegate = null
                loadAd()
                onAdDismissed()
            }

            override fun adWillPresentFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {}
            override fun adDidRecordImpression(ad: GADFullScreenPresentingAdProtocol) {}
            override fun adDidRecordClick(ad: GADFullScreenPresentingAdProtocol) {}
        }

        ad.fullScreenContentDelegate = currentDelegate
        ad.presentFromRootViewController(viewController)
    }

    actual fun isAdLoaded(): Boolean = interstitialAd != null
}

actual fun createInterstitialAdManager(): InterstitialAdManager = InterstitialAdManager()
```

---

## Desktop/Web заглушки

На платформах без рекламы создаются no-op реализации.

### jvmMain/.../ads/BannerAdView.jvm.kt

```kotlin
package com.yourpackage.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun BannerAdView(modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(50.dp))
}
```

### jvmMain/.../ads/InterstitialAdManager.jvm.kt

```kotlin
package com.yourpackage.ads

actual class InterstitialAdManager {
    actual fun loadAd() {}
    actual fun showAd(onAdDismissed: () -> Unit) { onAdDismissed() }
    actual fun isAdLoaded(): Boolean = false
}

actual fun createInterstitialAdManager(): InterstitialAdManager = InterstitialAdManager()
```

> Аналогично для `webMain` — те же файлы с тем же содержимым.

---

## Интеграция в App

### Сигнатура App()

```kotlin
@Composable
fun App(isSdkReady: Boolean = true)
```

- На **Android** — `isSdkReady` всегда `true` (SDK инициализируется синхронно в `onCreate`).
- На **iOS** — `isSdkReady` меняется на `true` после завершения `GADMobileAds.startWithCompletionHandler`.

### Логика показа Interstitial при навигации

```kotlin
@Composable
private fun AppContent(isSdkReady: Boolean) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var adLoaded by remember { mutableStateOf(false) }
    val adManager = remember { createInterstitialAdManager() }

    // Загрузка первого интерстишла когда SDK готов
    LaunchedEffect(isSdkReady) {
        if (isSdkReady && !adLoaded) {
            adManager.loadAd()
            adLoaded = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { selectedScreen ->
                    if (selectedScreen != currentScreen) {
                        // Показать интерстишл, затем навигация
                        adManager.showAd {
                            currentScreen = selectedScreen
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // ... рендеринг экранов ...
    }
}
```

**Ключевые моменты:**
- `showAd` вызывает `onAdDismissed` даже если реклама не загружена — навигация не блокируется.
- После показа рекламы автоматически вызывается `loadAd()` для предзагрузки следующей.

### Использование Banner на экранах

```kotlin
@Composable
fun MyScreen(modifier: Modifier = Modifier) {
    Scaffold(
        bottomBar = {
            BannerAdView()
        }
    ) { padding ->
        // Контент экрана
    }
}
```

---

## Использование на экранах

Баннер размещается в `bottomBar` каждого `Scaffold`:

```kotlin
import com.yourpackage.ads.BannerAdView

@Composable
fun AnyScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        bottomBar = { BannerAdView() }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // содержимое экрана
        }
    }
}
```

---

## Чеклист перед релизом

### Android
- [ ] Заменить тестовый `APPLICATION_ID` в `AndroidManifest.xml` на реальный
- [ ] Заменить тестовые Ad Unit ID (banner + interstitial) на реальные
- [ ] Убедиться что `com.google.android.gms:play-services-ads` в зависимостях
- [ ] Проверить что `INTERNET` permission есть в манифесте

### iOS
- [ ] Заменить тестовый `GADApplicationIdentifier` в `Info.plist` на реальный
- [ ] Заменить тестовые Ad Unit ID (banner + interstitial) на реальные
- [ ] Добавить `NSUserTrackingUsageDescription` в `Info.plist`
- [ ] Добавить `SKAdNetworkItems` в `Info.plist` (50+ идентификаторов от Google — без них реклама в проде почти не показывается)
- [ ] Запустить `pod install` после добавления `Google-Mobile-Ads-SDK`
- [ ] Убедиться что ATT запрос работает (iOS 14+)
- [ ] Использовать `connectedScenes` API вместо deprecated `keyWindow` в `ViewControllerProvider`

### Тестовые Ad Unit ID (Google)

| Тип | Android | iOS |
|-----|---------|-----|
| Banner | `ca-app-pub-3940256099942544/6300978111` | `ca-app-pub-3940256099942544/2934735716` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` | `ca-app-pub-3940256099942544/4411468910` |
| App ID | `ca-app-pub-3940256099942544~3347511713` | `ca-app-pub-3940256099942544~1458002511` |

### Важные нюансы

1. **iOS делегат** (`GADFullScreenContentDelegateProtocol`) должен храниться как свойство класса (`currentDelegate`), иначе Kotlin/Native GC может его собрать до вызова коллбэков.

2. **ActivityProvider** использует `WeakReference` чтобы не удерживать Activity и избежать утечек памяти. Обновляется в `onResume`.

3. **ATT запрос** должен вызываться **до** инициализации Google Mobile Ads SDK на iOS.

4. **`showAd` всегда вызывает `onAdDismissed`** — если реклама не загружена, коллбэк срабатывает немедленно, не блокируя пользовательский флоу.

5. **Предзагрузка**: после показа интерстишла автоматически вызывается `loadAd()` для подготовки следующего показа.

6. **SKAdNetwork IDs** — без них в `Info.plist` AdMob в проде будет возвращать пустые ответы (no fill). Это 50+ идентификаторов рекламных сетей. Актуальный список: https://developers.google.com/admob/ios/3p-skadnetworks

7. **`keyWindow` deprecated** — на iOS 15+ с multi-scene приложениями `UIApplication.sharedApplication.keyWindow` может вернуть `nil`. Используйте `connectedScenes` API (см. `ViewControllerProvider` в секции iOS).
