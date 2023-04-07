@file:Suppress("Deprecation")

package com.teachmeprint.language.feature.screenshot.presentation.ui

import android.content.Intent.EXTRA_STREAM
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily.ROUNDED
import com.google.android.material.shape.MaterialShapeDrawable
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.teachmeprint.language.R
import com.teachmeprint.language.core.helper.*
import com.teachmeprint.language.core.helper.StatusMessage.getErrorMessage
import com.teachmeprint.language.core.util.limitCharactersWithEllipsize
import com.teachmeprint.language.core.util.setOnItemSelectedWithDebounceListener
import com.teachmeprint.language.core.util.snackBarAlert
import com.teachmeprint.language.data.model.screenshot.TypeIndicatorEnum
import com.teachmeprint.language.data.model.screenshot.TypeIndicatorEnum.LISTEN
import com.teachmeprint.language.data.model.screenshot.TypeIndicatorEnum.TRANSLATE
import com.teachmeprint.language.databinding.ActivityScreenShotBinding
import com.teachmeprint.language.feature.screenshot.presentation.ScreenShotViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScreenShotActivity : AppCompatActivity(), CropImageView.OnCropImageCompleteListener {

    private val binding by lazy {
        ActivityScreenShotBinding.inflate(layoutInflater)
    }

    private val imageUriPath by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_STREAM)
        }
    }

    private val viewModel: ScreenShotViewModel by viewModels()

    @Inject
    lateinit var screenShotFloatingWindow: ScreenShotFloatingWindow

    @Inject
    lateinit var mobileAdsFacade: MobileAdsFacade

    private val requestPickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.cropImageScreenShot.setImageUriAsync(uri)
                setupImageCropOptions()
            } else {
                Timber.d(NO_MEDIA_SELECTED)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideSystemUI()
        setupBottomNavigation()
        setupCropImage()
        setupObservable()
    }

    override fun onResume() {
        super.onResume()
        screenShotFloatingWindow.showOrHide(false)
    }

    override fun onPause() {
        super.onPause()
        screenShotFloatingWindow.showOrHide()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun setupObservable() {
        viewModel.response.setLifecycleOwner(this)
            .observeOnSuccess { data ->
                performActionWithLoadingIndicator {
                    showTranslateOrOpenAds(data)
                }
            }
            .observeOnError {
                performActionWithLoadingIndicator()
                binding.bottomNavigationScreenShot.snackBarAlert(getErrorMessage(it))
            }
            .observeOnLoading {
                performActionWithLoadingIndicator(true)
            }
    }

    private fun showTranslateOrOpenAds(text: String) {
        if (!viewModel.hasReachedMaxTranslationCount()) {
            setupBalloonTranslate(text)
            return
        }
        mobileAdsFacade.setupInterstitialAds(this)
    }

    private fun setupBalloonTranslate(text: String) =
        with(Balloon.Builder(this)) {
            setWidthRatio(BALLOON_WIDTH_RATIO)
            setHeight(BalloonSizeSpec.WRAP)
            setText(text.limitCharactersWithEllipsize(BALLOON_LIMIT_CHARACTERS))
            setTextSize(BALLOON_TEXT_SIZE)
            setMarginHorizontal(BALLOON_MARGIN_HORIZONTAL)
            setMarginBottom(BALLOON_MARGIN_BOTTOM)
            setPadding(BALLOON_PADDING)
            setCornerRadius(BALLOON_CORNER_RADIUS)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setBackgroundColor(getColor(this@ScreenShotActivity, R.color.balloon_translate_color))
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            build().also {
                it.showAlignTop(binding.bottomNavigationScreenShot.findViewById(R.id.ic_translate))
            }
        }

    private fun setupDialogChooseLanguage() {
        lifecycleScope.launch {
            var languageSelectedIndex = viewModel.getLanguageSelectedIndex()

            MaterialAlertDialogBuilder(this@ScreenShotActivity)
                .setTitle(getString(R.string.text_title_dialog_choose_language))
                .setSingleChoiceItems(
                    viewModel.getLanguageList().toTypedArray(),
                    languageSelectedIndex
                ) { _, index ->
                    languageSelectedIndex = index
                }
                .setPositiveButton(getString(R.string.text_button_select_dialog_choose_language)) { dialog, _ ->
                    viewModel.saveLanguage { viewModel.getLanguageList()[languageSelectedIndex] }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.text_button_dialog_cancel)) { _, _ -> }
                .show()
        }
    }

    private fun setupBottomNavigation() =
        with(binding.bottomNavigationScreenShot) {
            setupShapeDrawableBottomNavigation()
            setOnItemSelectedWithDebounceListener { item ->
                when (item.itemId) {
                    R.id.ic_translate -> {
                        viewModel.getLanguage()?.let {
                            performActionIndicator(TRANSLATE)
                        } ?: run {
                            setupFirstChooseLanguage()
                        }
                    }
                    R.id.ic_listen -> {
                        performActionIndicator(LISTEN)
                    }
                    R.id.ic_reset_focus -> {
                        resetImageCropReact()
                    }
                    R.id.ic_image_gallery -> {
                        requestPickMedia.launch(PickVisualMediaRequest(ImageOnly))
                    }
                    R.id.ic_speak -> {
                        setupDialogChooseLanguage()
                    }
                }
                super.onOptionsItemSelected(item)
            }
        }

    private fun BottomNavigationView.setupShapeDrawableBottomNavigation() {
        val shapeDrawable = background as MaterialShapeDrawable

        shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel
            .toBuilder()
            .setAllCorners(ROUNDED, resources.getDimension(R.dimen.space_size_xlarge))
            .build()
    }

    private fun BottomNavigationView.setupFirstChooseLanguage() {
        snackBarAlert(
            R.string.text_select_language_speak_message,
            R.string.text_button_action_select_snack_bar, true
        ) {
            setupDialogChooseLanguage()
        }
    }

    private fun setupCropImage() {
        binding.cropImageScreenShot.setImageUriAsync(imageUriPath)
        binding.cropImageScreenShot.setOnCropImageCompleteListener(this)
        binding.cropImageScreenShot.cropRect =
            Rect(resources.displayMetrics.widthPixels - RECT_CUSTOM_RIGHT,
                RECT_CUSTOM_TOP,
                resources.displayMetrics.widthPixels, RECT_CUSTOM_BOTTOM)
        setupImageCropOptions()
    }

    private fun performActionIndicator(typeIndicatorEnum: TypeIndicatorEnum) {
        if (!binding.animationScreenShotLoadingTranslate.isVisible &&
            !binding.animationScreenShotLoadingListen.isVisible
        ) {
            viewModel.typeIndicatorEnum = typeIndicatorEnum
            binding.cropImageScreenShot.croppedImageAsync()
        }
    }

    private fun performActionWithLoadingIndicator(
        isVisible: Boolean = false,
        onActionTranslate: () -> Unit = {}
    ) {
        when (viewModel.typeIndicatorEnum) {
            TRANSLATE -> {
                binding.animationScreenShotLoadingTranslate.isVisible = isVisible
                onActionTranslate.invoke()
            }
            LISTEN -> {
                binding.animationScreenShotLoadingListen.isVisible = isVisible
            }
        }
    }

    private fun setupImageCropOptions() {
        val cropImageOptions = CropImageOptions(
            guidelines = CropImageView.Guidelines.OFF,
            cornerShape = CropImageView.CropCornerShape.OVAL,
            showProgressBar = false
        )
        binding.cropImageScreenShot.setImageCropOptions(cropImageOptions)
    }

    private fun resetImageCropReact() {
        binding.cropImageScreenShot.cropRect = Rect(null)
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        viewModel.fetchTextRecognizer(result.bitmap)
    }

    private fun hideSystemUI() {
        changeStatusBarColor()
        lifecycleScope.launch {
            delay(DELAY_STATUS_BAR_HIDE)
            WindowInsetsControllerCompat(window, binding.root).apply {
                hide(WindowInsetsCompat.Type.statusBars())
                systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun changeStatusBarColor() {
        val window = window
        window.clearFlags(FLAG_TRANSLUCENT_STATUS)
        window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(this, R.color.black)
    }

    override fun onDestroy() {
        viewModel.stopTextToSpeech()
        super.onDestroy()
    }

    companion object {
        private const val NO_MEDIA_SELECTED = "No media selected."

        private const val BALLOON_WIDTH_RATIO = 0.90f
        private const val BALLOON_TEXT_SIZE = 14F
        private const val BALLOON_MARGIN_HORIZONTAL = 16
        private const val BALLOON_MARGIN_BOTTOM = 8
        private const val BALLOON_PADDING = 12
        private const val BALLOON_CORNER_RADIUS = 24F
        private const val BALLOON_LIMIT_CHARACTERS = 280

        private const val RECT_CUSTOM_RIGHT = 500
        private const val RECT_CUSTOM_BOTTOM = 450
        private const val RECT_CUSTOM_TOP = 0

        private const val DELAY_STATUS_BAR_HIDE = 1000L
    }
}