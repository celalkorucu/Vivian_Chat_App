package com.celalkorucu.xxx.Adapter
import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.celalkorucu.xxx.Activitys.ChatActivity
import com.celalkorucu.xxx.Manager.ToUserManager
import com.celalkorucu.xxx.Model.Talk
import com.celalkorucu.xxx.Model.User
import com.celalkorucu.xxx.R
import com.celalkorucu.xxx.databinding.TalksRecyclerRowBinding
import com.makeramen.roundedimageview.RoundedImageView
import java.util.Calendar
import java.util.Locale

class TalksAdapter (var myUser : User, private val list : List<Talk>): RecyclerView.Adapter<TalksAdapter.Holder>() {
    private lateinit var toUserManager: ToUserManager

    class Holder(val binding: TalksRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            TalksRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.talksProfilePhoto.setImageBitmap(list[position].talkUser.profilePhoto)
        holder.binding.talksUserName.text = list[position].talkUser.username

        if(list[position].lastMessage.senderUser == myUser.username){
            holder.binding.talksLastMessageText.text = "Siz : ${list[position].lastMessage.messageText}"
        }else{
            holder.binding.talksLastMessageText.text = list[position].lastMessage.messageText
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(list[position].lastMessage.timeStamp)


        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val dmy = "$day.$month.$year"
        val hm = "$hours:$minute"


        holder.binding.talksLastMessageDate.text = dmy
        holder.binding.talksLastMessageTime.text = hm


        //Talks Item Click
        holder.itemView.setOnClickListener {
            talksItemClick(holder, position)
        }

        holder.binding.talksProfilePhoto.setOnClickListener {
            showProfilePhoto(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Profil fotoğrafını büyüten metod
    private fun showProfilePhoto(holder: Holder, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)

        // Büyük profil fotoğrafını gösteren ImageView

        val imageView = RoundedImageView(holder.itemView.context)
        imageView.setImageBitmap(list[position].talkUser.profilePhoto) // Büyük görüntü kaynağını belirtin
        imageView.background = getDrawable(
            holder.itemView.context,
            R.drawable.background_show_profile_photo_
        );
        imageView.adjustViewBounds = true

        builder.setView(imageView)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    //toUser nesnesinin initialize eden metod
    private fun talksItemClick(holder: Holder, position: Int) {
        //Go To Chat Activity
        toUserManager = ToUserManager.getInstance()
        val chatUser = User(list[position].talkUser.profilePhoto, list[position].talkUser.username)
        toUserManager.setCurrentUser(chatUser)
        val intent = Intent(holder.itemView.context, ChatActivity::class.java)
        holder.itemView.context.startActivity(intent)
    }
}