package com.celalkorucu.xxx.Helper

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import com.celalkorucu.xxx.R

public class DialogHelper {

    companion object {

        fun alertBuilder(context : Context) : AlertDialog.Builder{
            return AlertDialog.Builder(ContextThemeWrapper(context , R.style.TemaDuzenle))
        }

        fun newChatBuilder(context: Context) : AlertDialog.Builder{
            return AlertDialog.Builder(ContextThemeWrapper(context , R.style.new_chat_style))
        }
    }
}