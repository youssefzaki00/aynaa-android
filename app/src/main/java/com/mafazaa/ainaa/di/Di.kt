package com.mafazaa.ainaa.di

import android.content.Context.MODE_PRIVATE
import com.mafazaa.ainaa.viewmodels.AppViewModel
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.Constants
import com.mafazaa.ainaa.data.JsEngine
import com.mafazaa.ainaa.data.local.RealFileRepo
import com.mafazaa.ainaa.data.UpdateManager
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.remote.FakeRemoteRepo
import com.mafazaa.ainaa.data.remote.KtorRepo
import com.mafazaa.ainaa.data.ContentRepoImpl
import com.mafazaa.ainaa.data.LuaScriptRepo
import com.mafazaa.ainaa.data.data_source.LocalContentImpl
import com.mafazaa.ainaa.data.data_source.LocalContentRepo
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.domain.repo.ContentRepo
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.domain.repo.UpdateRepo
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<RemoteRepo> { if (BuildConfig.DEBUG) FakeRemoteRepo else KtorRepo() }
    single<SharedPrefs> { SharedPrefs(androidContext().getSharedPreferences("App", MODE_PRIVATE)) }
    single<FileRepo> { RealFileRepo(androidContext()) }
    single<LockOverlayManager> { LockOverlayManager(androidContext()) }
    single<ScreenshotOverlayManager> { ScreenshotOverlayManager(androidContext()) }
    single<LocalContentRepo>{ LocalContentImpl( androidContext().getSharedPreferences("localContent", MODE_PRIVATE)) }
    single<ScriptRepo> {
        LuaScriptRepo().apply {
            setCodes(LuaScriptRepo.defaultScripts)
        }
    }
    single<UpdateRepo> { UpdateManager(get(), get(), get()) }
    single<ContentRepo> { ContentRepoImpl(get(), get()) }

    viewModel { AppViewModel(get(), get(), get(), get(), get(),get()) }

}
