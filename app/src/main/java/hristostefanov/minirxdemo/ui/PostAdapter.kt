package hristostefanov.minirxdemo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hristostefanov.minirxdemo.R
import hristostefanov.minirxdemo.business.PostFace
import kotlinx.android.synthetic.main.post_list_item.view.*

class PostAdapter(private val list: List<PostFace>): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = list[position]
        with(holder) {
            usernameTextView.text = post.username
            titleTextView.text = post.title
        }
    }

    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.usernameTextView
        val titleTextView: TextView = itemView.titleTextView
    }
}
