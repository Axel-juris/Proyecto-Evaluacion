plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.example.proyecto_evaluacion"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.proyecto_evaluacion"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Las clases de android.jar devuelven valores por defecto en vez de lanzar "not mocked"
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)

    // Consumo de microservicios y APIs externas
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(kotlin("test"))
}

// ---------------------------------------------------------------------------
// Cobertura de pruebas unitarias con Kover (guía DSY1105)
//   Reporte HTML : ./gradlew :app:koverHtmlReportDebug
//                  -> app/build/reports/kover/htmlDebug/index.html
//   Verificación : ./gradlew :app:koverVerifyDebug   (falla si la cobertura < 80 %)
//
// Se mide solo la lógica: ViewModels, Repository, validadores, mapeo de errores,
// sesión y utilidades de fecha. No se cuenta la presentación visual (screens,
// components, navegacion, ui.theme), MainActivity, RetrofitClient (configuración),
// ni las clases que dependen del sistema Android (Calendario, Evidencias).
// ---------------------------------------------------------------------------
kover {
    reports {
        filters {
            includes {
                packages(
                    "com.example.proyecto_evaluacion.validation",
                    "com.example.proyecto_evaluacion.viewmodel"
                )
                classes(
                    "com.example.proyecto_evaluacion.data.CrmRepository*",
                    "com.example.proyecto_evaluacion.data.FeriadosRepository*",
                    "com.example.proyecto_evaluacion.data.ApiErrorMapperKt*",
                    "com.example.proyecto_evaluacion.data.SessionManager*",
                    "com.example.proyecto_evaluacion.data.ErrorAplicacion*",
                    "com.example.proyecto_evaluacion.fechas.Fechas*",
                    "com.example.proyecto_evaluacion.fechas.FeriadosHelper*"
                )
            }
        }
        verify {
            rule("Cobertura minima de logica") {
                minBound(80)
            }
        }
    }
}