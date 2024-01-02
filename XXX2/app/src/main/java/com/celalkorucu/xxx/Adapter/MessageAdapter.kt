package com.celalkorucu.xxx.Adapter

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.celalkorucu.xxx.Model.Message
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MessageAdapter (var myUser : User, var list : ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.Holder>() {


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun getItemViewType(position: Int): Int {
        if(list  != null){
            val chat  = list[position]
            if(chat.senderUser == myUser.username){
                return 1
            }else {
                return 2
            }
        }else{
            return 1
        }
    }
    //1 benim attığım mesajlar
    //2 karşıdan gelen mesajlar

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if(viewType == 1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_send_message, parent , false)
            return Holder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_received_message, parent , false)
            return Holder(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {

        if(list.isNotEmpty()){
            if(list[position].senderUser == myUser.username){
                val textView = holder.itemView.findViewById<TextView>(R.id.sendMessageText)
                val timeText = holder.itemView.findViewById<TextView>(R.id.sendMessageTime)


                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(list[position].timeStamp)

                val calendar = Calendar.getInstance()
                calendar.time = date

                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val time = "$hours:$minute"


                textView.text = list[position].messageText
                timeText.text = time

            }else{
                val textView = holder.itemView.findViewById<TextView>(R.id.sendReceivedMessageText)
                val timeText = holder.itemView.findViewById<TextView>(R.id.sendReceivedMessageTime)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(list[position].timeStamp)

                val calendar = Calendar.getInstance()
                calendar.time = date


                val hours = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val time = "$hours:$minute"


                textView.text = list[position].messageText
                timeText.text = time


                textView.text = list[position].messageText
                timeText.text = time
            }
        }else{
            Toast.makeText(holder.itemView.context , "Hata" , Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }
}