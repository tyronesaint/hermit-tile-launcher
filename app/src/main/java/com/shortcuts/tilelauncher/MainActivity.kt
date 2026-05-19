package com.shortcuts.tilelauncher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var etName: EditText
    private lateinit var etUrl: EditText
    private lateinit var etKey: EditText
    private lateinit var tvIconPreview: TextView
    private lateinit var ivIconPreview: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnTest: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnSelectIcon: Button
    private lateinit var btnExport: Button
    private lateinit var btnImport: Button
    private lateinit var cardPreview: MaterialCardView

    private var currentTile = 1
    private var currentIcon = "search"
    private var currentIconType = IconType.PRESET
    private var currentIconScale = IconHelper.Scale.MEDIUM

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val ok = ConfigBackupManager.exportConfig(this, uri)
                Toast.makeText(this, if (ok) "导出成功" else "导出失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val ok = ConfigBackupManager.importConfig(this, uri)
                Toast.makeText(this, if (ok) "导入成功" else "导入失败", Toast.LENGTH_SHORT).show()
                if (ok) loadTile(currentTile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupListeners()
        loadTile(currentTile)
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        etName = findViewById(R.id.etName)
        etUrl = findViewById(R.id.etUrl)
        etKey = findViewById(R.id.etKey)
        tvIconPreview = findViewById(R.id.tvIconPreview)
        ivIconPreview = findViewById(R.id.ivIconPreview)
        btnSave = findViewById(R.id.btnSave)
        btnTest = findViewById(R.id.btnTest)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnSelectIcon = findViewById(R.id.btnSelectIcon)
        btnExport = findViewById(R.id.btnExport)
        btnImport = findViewById(R.id.btnImport)
        cardPreview = findViewById(R.id.cardPreview)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val url = etUrl.text.toString().trim()
            val key = etKey.text.toString().trim()

            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "名称和网址不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTileConfig(this, currentTile, name, url, key, currentIcon, currentIconType, currentIconScale)
            Toast.makeText(this, "Tile $currentTile 已保存", Toast.LENGTH_SHORT).show()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.service.quicksettings.TileService.requestListeningState(
                    this,
                    android.content.ComponentName(this, when(currentTile) {
                        1 -> TileService1::class.java
                        2 -> TileService2::class.java
                        3 -> TileService3::class.java
                        4 -> TileService4::class.java
                        5 -> TileService5::class.java
                        else -> TileService6::class.java
                    })
                )
            }
        }

        btnTest.setOnClickListener {
            val url = etUrl.text.toString().trim()
            val key = etKey.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "先填网址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HermitLauncher.launch(this, url, key)
        }

        btnPrev.setOnClickListener {
            saveCurrent()
            if (currentTile > 1) {
                currentTile--
                loadTile(currentTile)
            }
        }

        btnNext.setOnClickListener {
            saveCurrent()
            if (currentTile < 6) {
                currentTile++
                loadTile(currentTile)
            }
        }

        btnSelectIcon.setOnClickListener { showIconTypeSelector() }

        btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, ConfigBackupManager.generateBackupFileName())
            }
            createDocumentLauncher.launch(intent)
        }

        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            openDocumentLauncher.launch(intent)
        }
    }

    /** 选择图标类型 */
    private fun showIconTypeSelector() {
        val options = arrayOf("内置图标 (50+)", "Emoji 表情", "透图文字")
        MaterialAlertDialogBuilder(this)
            .setTitle("选择图标类型")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showPresetIconPicker()
                    1 -> showEmojiInput()
                    2 -> showTransparentInput()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 选择内置图标 */
    private fun showPresetIconPicker() {
        val icons = IconHelper.PRESET_ICONS
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_icon_picker, null)
        val gridView = dialogView.findViewById<GridView>(R.id.gridIcons)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("选择内置图标")
            .setView(dialogView)
            .setNegativeButton("取消", null)
            .create()

        gridView.adapter = object : BaseAdapter() {
            override fun getCount() = icons.size
            override fun getItem(position: Int) = icons[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.item_icon, parent, false)

                val (key, label) = icons[position]
                val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
                val tvName = view.findViewById<TextView>(R.id.tvIconName)

                val bitmap = IconHelper.icon(this@MainActivity, key, IconHelper.Scale.MEDIUM).loadDrawable(this@MainActivity)?.let {
                    val bmp = android.graphics.Bitmap.createBitmap(96, 96, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    it.setBounds(0, 0, 96, 96)
                    it.draw(canvas)
                    bmp
                }
                ivIcon.setImageBitmap(bitmap)
                tvName.text = label

                view.setOnClickListener {
                    showScalePicker { scale ->
                        currentIcon = key
                        currentIconType = IconType.PRESET
                        currentIconScale = scale
                        updateIconPreview()
                        Toast.makeText(this@MainActivity, "图标: $label (${scale.name})", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
                return view
            }
        }

        dialog.show()
    }

    /** 输入 Emoji */
    private fun showEmojiInput() {
        val input = EditText(this)
        input.hint = "输入Emoji，例如: 🚀 🔥 🐱"
        input.maxLines = 1
        MaterialAlertDialogBuilder(this)
            .setTitle("自定义 Emoji")
            .setView(input)
            .setPositiveButton("下一步") { _, _ ->
                val emoji = input.text.toString().trim()
                if (emoji.isNotEmpty()) {
                    showScalePicker { scale ->
                        currentIcon = emoji
                        currentIconType = IconType.EMOJI
                        currentIconScale = scale
                        updateIconPreview()
                        Toast.makeText(this, "Emoji: $emoji (${scale.name})", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 输入透图文字 */
    private fun showTransparentInput() {
        val input = EditText(this)
        input.hint = "输入1-3个字符"
        input.maxLines = 1
        MaterialAlertDialogBuilder(this)
            .setTitle("自定义透图文字")
            .setView(input)
            .setPositiveButton("下一步") { _, _ ->
                val text = input.text.toString().trim().take(3)
                if (text.isNotEmpty()) {
                    showScalePicker { scale ->
                        currentIcon = text
                        currentIconType = IconType.TRANSPARENT
                        currentIconScale = scale
                        updateIconPreview()
                        Toast.makeText(this, "透图: '$text' (${scale.name})", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 选择内容占比 */
    private fun showScalePicker(onPick: (IconHelper.Scale) -> Unit) {
        val scales = arrayOf("小 (30%)", "中 (60%)", "大 (85%)", "满铺 (100%)")
        MaterialAlertDialogBuilder(this)
            .setTitle("选择内容占比")
            .setItems(scales) { _, which ->
                val scale = when (which) {
                    0 -> IconHelper.Scale.SMALL
                    1 -> IconHelper.Scale.MEDIUM
                    2 -> IconHelper.Scale.LARGE
                    else -> IconHelper.Scale.FULL
                }
                onPick(scale)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updateIconPreview() {
        val icon = when (currentIconType) {
            IconType.PRESET -> {
                tvIconPreview.text = "图标: ${IconHelper.PRESET_ICONS.find { it.first == currentIcon }?.second ?: currentIcon} (${currentIconScale.name})"
                IconHelper.icon(this, currentIcon, currentIconScale)
            }
            IconType.EMOJI -> {
                tvIconPreview.text = "Emoji: $currentIcon (${currentIconScale.name})"
                IconHelper.emojiIcon(this, currentIcon, currentIconScale)
            }
            IconType.TRANSPARENT -> {
                tvIconPreview.text = "透图: '$currentIcon' (${currentIconScale.name})"
                IconHelper.transparentBitmap(this, currentIcon, currentIconScale)
            }
        }
        ivIconPreview.setImageIcon(icon)
    }

    private fun saveCurrent() {
        val name = etName.text.toString().trim()
        val url = etUrl.text.toString().trim()
        val key = etKey.text.toString().trim()
        saveTileConfig(this, currentTile, name, url, key, currentIcon, currentIconType, currentIconScale)
    }

    private fun loadTile(tileId: Int) {
        val cfg = loadTileConfig(this, tileId)
        etName.setText(cfg.name)
        etUrl.setText(cfg.url)
        etKey.setText(cfg.key)
        currentIcon = cfg.icon
        currentIconType = cfg.iconType
        currentIconScale = cfg.iconScale
        tvStatus.text = "正在配置: Tile $tileId / 6"
        updateIconPreview()

        // 更新导航按钮状态
        btnPrev.isEnabled = tileId > 1
        btnNext.isEnabled = tileId < 6
        btnPrev.alpha = if (tileId > 1) 1.0f else 0.5f
        btnNext.alpha = if (tileId < 6) 1.0f else 0.5f
    }
}
