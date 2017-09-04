package me.proxer.app.util.extension

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

inline val Context.inputMethodManager: InputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

inline val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

inline val Context.clipboardManager: ClipboardManager
    get() = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

inline val Context.notificationManager: NotificationManager
    get() = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
