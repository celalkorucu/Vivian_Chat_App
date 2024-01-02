package com.celalkorucu.xxx.Activitys

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.celalkorucu.xxx.Helper.DialogHelper
import com.celalkorucu.xxx.R
import com.celalkorucu.xxx.databinding.NewChatBinding


class NewChatFragment : DialogFragment() {

    private lateinit var binding : NewChatBinding
    private lateinit var listener: AddTalkListener

    interface AddTalkListener {
        fun okAddTalk(username: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as AddTalkListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement AddTalkListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = DialogHelper.newChatBuilder(requireActivity())
        binding = NewChatBinding.inflate(layoutInflater)
        val view = binding.root

        builder.setView(view)

            .setTitle("Enter Username")
            .setIcon(R.drawable.talks_add)
            .setPositiveButton("Start Chat") { _, _ ->
                val name = binding.newChatUsername.text.toString()
                listener.okAddTalk(name)

            }
            .setNegativeButton("Cancel") { _, _ ->
                // İptal butonuna tıklandığında bir şey yapabilirsiniz.
            }

        return builder.create()
    }
}
