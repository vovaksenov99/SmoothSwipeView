package com.aksenov.swiper

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aksenov.swiper.utils.px
import com.aksenov.swiper.views.SwipeCardItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_game_screen.*
import kotlinx.android.synthetic.main.view_actor_card.view.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_game_screen)
        swipe_view.onCardBind = { view: View, any: Any ->
            when (any) {
                is SomeUsefulData -> {
                    view.title.text = any.name
                    Glide
                        .with(this)
                        .load(any.resId)
                        .centerCrop()
                        .apply(
                            RequestOptions()
                                .override(500, 800)
                        )
                        .placeholder(android.R.color.white)
                        .into(view.image)

                    view.image.shapeAppearanceModel = view.image.shapeAppearanceModel
                        .toBuilder()
                        .setAllCornerSizes(12.px.toFloat())
                        .build()
                }
                else -> {
                    //You can add another view type
                }
            }
        }
        swipe_view.submitData(
            listOf(
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Mircella", R.drawable.mircella)),
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Frodo", R.drawable.frodo)),
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Aragorn", R.drawable.aragorn)),
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Arven", R.drawable.arven)),
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Davos", R.drawable.davos)),
                SwipeCardItem(R.layout.view_actor_card, SomeUsefulData("Eomer", R.drawable.eomer))
            )
        )
    }

    data class SomeUsefulData(
        val name: String,
        val resId: Int
    )

}