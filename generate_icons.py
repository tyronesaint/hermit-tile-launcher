#!/usr/bin/env python3
"""Generate tile icons for Hermit Tile Launcher."""

from PIL import Image, ImageDraw
import os

# 图标尺寸 (mipmap density)
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}

# 图标颜色配置 (4个不同的颜色)
tile_colors = {
    1: ((33, 150, 243), (255, 255, 255)),    # 蓝色 - 背景色, 文字色
    2: ((76, 175, 80), (255, 255, 255)),      # 绿色
    3: ((255, 152, 0), (255, 255, 255)),      # 橙色
    4: ((156, 39, 176), (255, 255, 255)),    # 紫色
}

def create_tile_icon(size, bg_color, text_color, number):
    """创建单个磁贴图标."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 计算圆角矩形参数
    margin = size // 10
    radius = size // 5
    
    # 绘制圆角矩形背景
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=radius,
        fill=bg_color
    )
    
    # 绘制数字 (作为图标)
    text_size = size // 2
    font_x = size // 2
    font_y = size // 2
    
    # 绘制圆形背景
    circle_radius = size // 3
    draw.ellipse(
        [font_x - circle_radius, font_y - circle_radius,
         font_x + circle_radius, font_y + circle_radius],
        fill=text_color
    )
    
    # 绘制数字
    number_str = str(number)
    text_bbox = draw.textbbox((0, 0), number_str)
    text_width = text_bbox[2] - text_bbox[0]
    text_height = text_bbox[3] - text_bbox[1]
    
    # 调整文字大小
    scale = (circle_radius * 1.2) / max(text_width, text_height)
    text_x = font_x - (text_width * scale) // 2
    text_y = font_y - (text_height * scale) // 2
    
    draw.text(
        (text_x, text_y),
        number_str,
        fill=bg_color
    )
    
    return img

def create_launcher_icon(size):
    """创建应用启动器图标."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 背景渐变效果 (简化版)
    bg_color = (33, 150, 243)
    margin = size // 8
    radius = size // 4
    
    # 绘制圆角矩形背景
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=radius,
        fill=bg_color
    )
    
    # 绘制 "H" 字母
    center = size // 2
    h_color = (255, 255, 255)
    
    # 竖线
    line_width = size // 8
    left = center - size // 4
    right = center + size // 4
    top = center - size // 4
    bottom = center + size // 4
    
    draw.rectangle([left, top, left + line_width, bottom], fill=h_color)
    draw.rectangle([right - line_width, top, right, bottom], fill=h_color)
    
    # 横线
    mid_top = center - line_width // 2
    mid_bottom = center + line_width // 2
    draw.rectangle([left, mid_top, right, mid_bottom], fill=h_color)
    
    return img

def main():
    # 创建目录
    base_dir = 'app/src/main/res'
    
    for density, size in sizes.items():
        # 创建 mipmap 目录
        mipmap_dir = f'{base_dir}/mipmap-{density}'
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # 生成磁贴图标 1-4
        for tile_num, (bg_color, text_color) in tile_colors.items():
            icon = create_tile_icon(size, bg_color, text_color, tile_num)
            icon.save(f'{mipmap_dir}/ic_tile{tile_num}.png')
        
        # 生成启动器图标
        launcher = create_launcher_icon(size)
        launcher.save(f'{mipmap_dir}/ic_launcher.png')
        launcher.save(f'{mipmap_dir}/ic_launcher_round.png')
    
    print("✓ 图标生成完成!")
    print("  - 4个磁贴图标: ic_tile1 ~ ic_tile4")
    print("  - 启动器图标: ic_launcher, ic_launcher_round")

if __name__ == '__main__':
    main()
