# SmoothSwipeView
*Min API 16*

Small customizable view with smooth swipe. There's nothing extra. Supports different view's types.

View has view pool optimizations, dynamic addition/removing. Has fine performance. Supports multi-touch.

Project sample is above.

# Work example
![](/images/SVID-20200823-112252-1.gif)

# View params.

| Parameter | Description | units |
| ------------- | ------------- | ------------- |
| `onCardBind`  | Called when swipe card on screen should been drawn. Called once for each view and only when view added on screen View - view for initialization, Any - f.e data class with view initialization data | (View, Any) -> Unit |
| `onAllCardSwiped`  | Called when card to swipe is running out  | () -> Unit |
| `onCardSwiped`  | Called for card that was swiped. View - view that was swiped, SwipeDirection - LEFT or RIGHT, Any - view initialization data from `onCardBind`.    | (View, Any?, SwipeDirection) -> Unit |
| `onCardSwipingPercentChanged`  |  Called when you drag view. Float - is value from 0 to 1 where 0 - view on start position, 1 - view dragged enough for swipe if user release it. SwipeDirection - LEFT or RIGHT | (Float, SwipeDirection?) -> Unit |
| `fun swipe`  | You can programatically call view swiping  | - |
| `fun submitData`  | Function for submit data to display  | - |

Also view has several constants, which you can change for customize view behaviour

| Parameter | def. value | description |
| ------------- | ------------- | ------------- |
| MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_X |  150f * screenDensity | Max distance where view follow toward finger without deceleration along x axis |
| MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_Y |  50f * screenDensity | Max distance where view follow toward finger without deceleration along y axis |
| OVERSCREEN_SPACE_X |  Resources.getSystem().displayMetrics.widthPixels + 200f | X position where swiped view removing |
| MIN_SWIPE_BARRIER_X |  80f * screenDensity | X position where view will be swiped if user release finger |
| MAX_ANGLE |  20f | Max view incline whilst swiping  |
| MIN_DRAG_DECELERATION_SPEED_COEFFCIENT |  0.7f | Show how speed will be decrease after overcome MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_X or MAX_DISTANCE_TO_MOVE_WITHOUT_RESTRICTION_Y bounds |
| COMPLETE_SWIPE_ANIMATION_DURATION_MS |  300L | Animation duration when view was swiped |
| AUTOSWIPE_ANIMATION_DURATION_MS |  400L | Animation duration when view was swiped programmatically |
| EXIT_FROM_SCREEN_CARD_ANIMATION_DURATION_MS |  300L | Animation duration that restoring start view position when it was released before reach swipe's bound |
| LAYOUT_CARD_STACK_SIZE |  4 | Permanent card quantity on screen. Should be more that 1. You need card for display when top view is swiping |
