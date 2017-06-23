package me.proxer.app.application

import android.content.Context
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import me.proxer.app.application.MainApplication.Companion.client
import java.io.InputStream

/**
 * @author Ruben Gees
 */
@GlideModule
class ProxerGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }

    override fun isManifestParsingEnabled() = false
}