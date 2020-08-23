package com.aksenov.swiper.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.children
import com.aksenov.swiper.R
import com.aksenov.swiper.utils.ActionThrottler
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val screenDensity = Resources.getSystem().displayMetrics.density

        private val MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_X = 150f * screenDensity
        private val MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_Y = 50f * screenDensity
        private val OVERSCREEN_SPACE_X = Resources.getSystem().displayMetrics.widthPixels + 200f
        private val MIN_SWIPE_BARRIER_X = 80f * screenDensity
        private const val MAX_ANGLE = 20f
        private const val MIN_DRAG_DECELERATION_SPEED_COEFFCIENT = 0.7f
        private const val COMPLETE_SWIPE_ANIMATION_DURATION_MS = 300L
        private const val AUTOSWIPE_ANIMATION_DURATION_MS = 400L
        private const val EXIT_FROM_SCREEN_CARD_ANIMATION_DURATION_MS = 300L
        private const val LAYOUT_CARD_STACK_SIZE = 4

    }

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }

    private val cardsQueue: Queue<SwipeCardItem> = LinkedList<SwipeCardItem>()
    private val displayedViews: Queue<Pair<View, Any?>> = LinkedList<Pair<View, Any?>>()

    private val cardViewLayoutsPool: MutableMap<Int, MutableList<View>> = mutableMapOf()

    private var topView: View? = null
    private var topData: Any? = null

    //You should define that before call submit
    var onCardBind: ((View, Any) -> Unit)? = null

    var onAllCardSwiped: (() -> Unit)? = null
    var onCardSwiped: ((View, Any?, SwipeDirection) -> Unit)? = null
    var onCardSwipingPercentChanged: ((Float, SwipeDirection?) -> Unit)? = null

    private fun View.setSwipeCard(data: Any) {
        var startRawX = 0f
        var startRawY = 0f

        var lastX = 0f
        var lastY = 0f

        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRawX = event.rawX
                    startRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val distX = event.rawX - startRawX
                    val distY = event.rawY - startRawY
                    val dx = (distX - lastX)
                    val dy = (distY - lastY)
                    val swipePercent = min(1f, abs(distX / MIN_SWIPE_BARRIER_X))

                    val interpolationCoefficientX =
                        AccelerateDecelerateInterpolator().getInterpolation(
                            min(abs(distX) / MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_X, 1f)
                        )
                    val decelerateCoefficientX =
                        min(MIN_DRAG_DECELERATION_SPEED_COEFFCIENT, interpolationCoefficientX)
                    view.x = view.x + (dx * (1 - decelerateCoefficientX))
                    if (distX > 0) {
                        view.rotation = min(MAX_ANGLE, distX / 35f)
                        onCardSwipingPercentChanged?.invoke(swipePercent, SwipeDirection.RIGHT)
                    } else {
                        view.rotation = max(-MAX_ANGLE, distX / 35f)
                        onCardSwipingPercentChanged?.invoke(swipePercent, SwipeDirection.LEFT)
                    }

                    val interpolationCoefficientY =
                        AccelerateDecelerateInterpolator().getInterpolation(
                            Math.min(abs(distY) / MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_Y, 1f)
                        )
                    val decelerateCoefficientY =
                        Math.min(MIN_DRAG_DECELERATION_SPEED_COEFFCIENT, interpolationCoefficientY)
                    view.y = view.y + (dy * (1 - decelerateCoefficientY))
                    lastX = distX
                    lastY = distY
                    true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    when {
                        lastX >= MIN_SWIPE_BARRIER_X -> {
                            swipe(SwipeDirection.RIGHT, data, view)
                        }
                        lastX <= -MIN_SWIPE_BARRIER_X -> {
                            swipe(SwipeDirection.LEFT, data, view)
                        }
                        else -> {
                            onCardSwipingPercentChanged?.invoke(0f, null)
                            view.animate()
                                .x(0f)
                                .y(0f)
                                .rotation(0f)
                                .setDuration(EXIT_FROM_SCREEN_CARD_ANIMATION_DURATION_MS)
                                .start()
                        }
                    }
                    lastX = 0f
                    lastY = 0f
                    true
                }
                else -> false
            }
        }
    }

    private fun swipe(
        swipeDirection: SwipeDirection,
        swipedData: Any?,
        swipedView: View?,
        animationDuration: Long = COMPLETE_SWIPE_ANIMATION_DURATION_MS
    ) {
        swipedView?.let { swipedView ->
            swipedView.setOnTouchListener { _, _ -> false }
            onCardSwiped?.invoke(swipedView, swipedData ?: topData, swipeDirection)
            swipedView.animate()
                .x(if (swipeDirection == SwipeDirection.LEFT) -OVERSCREEN_SPACE_X else OVERSCREEN_SPACE_X)
                .rotation(MAX_ANGLE * if (swipeDirection == SwipeDirection.LEFT) -1 else 1)
                .setDuration(animationDuration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    removeCardFromLayout(swipedView)
                    addCardToLayout(cardsQueue.poll())
                }
                .start()
        }
    }


    fun swipe(swipeDirection: SwipeDirection) {
        val insuranceAnimationDelayMs = 50
        ActionThrottler.throttleAction(AUTOSWIPE_ANIMATION_DURATION_MS + insuranceAnimationDelayMs) {
            swipe(
                swipeDirection,
                null,
                topView ?: getChildAt(LAYOUT_CARD_STACK_SIZE - 1),
                AUTOSWIPE_ANIMATION_DURATION_MS
            )
        }
    }

    fun submitData(list: List<SwipeCardItem>) {
        setBackgroundResource(R.drawable.swipe_view_background_loading)
        while (topView != null) {
            removeCardFromLayout(topView)
        }
        cardsQueue.clear()
        cardsQueue.addAll(list)

        for (i in 0 until LAYOUT_CARD_STACK_SIZE) {
            addCardToLayout(cardsQueue.poll())
        }
        topData = displayedViews.peek()?.second
        topView = displayedViews.poll()?.first

        if (displayedViews.size < LAYOUT_CARD_STACK_SIZE) {
            setBackgroundResource(R.drawable.swipe_view_background_done)
        }
    }

    private fun removeCardFromLayout(view: View?) {
        if (view == null) return
        removeView(view)
        if (displayedViews.size < LAYOUT_CARD_STACK_SIZE) setBackgroundResource(R.drawable.swipe_view_background_done)
        if (displayedViews.size == 0) onAllCardSwiped?.invoke()
        topData = displayedViews.peek()?.second
        topView = displayedViews.poll()?.first
    }

    private fun addCardToLayout(cardItem: SwipeCardItem?) {
        if (cardItem == null) return
        val view = getViewForDisplay(cardItem.layoutResId)
        addView(view, 0)
        displayedViews.add(view to cardItem.data)
        onCardBind?.invoke(view, cardItem.data)
        view.setSwipeCard(cardItem.data)
    }

    private fun getViewForDisplay(layoutResId: Int): View {
        val poolView = cardViewLayoutsPool[layoutResId]?.firstUnusedView()
        return if (poolView != null) {
            poolView.resetViewFromPool()
        } else {
            val inflatedView = layoutInflater.inflate(layoutResId, null)
            if (cardViewLayoutsPool[layoutResId] == null) {
                cardViewLayoutsPool[layoutResId] = mutableListOf(inflatedView)
            } else {
                cardViewLayoutsPool[layoutResId]?.add(inflatedView)
            }
            inflatedView
        }
    }

    private fun View.resetViewFromPool() =
        this.apply {
            x = 0f
            y = 0f
            rotation = 0f
        }

    private fun isViewPresentingOnScreen(view: View): Boolean {
        children.forEach {
            if (it == view) return true
        }
        return false
    }

    private fun MutableList<View>.firstUnusedView(): View? {
        forEach { view ->
            if (!isViewPresentingOnScreen(view)) return view
        }
        return null
    }

}

data class SwipeCardItem(
    @LayoutRes val layoutResId: Int,
    val data: Any
)

enum class SwipeDirection {
    LEFT,
    RIGHT
}