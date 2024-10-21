# shapeable

ShapeableDrawable/ShapeableLinearLayout/ShapeableFrameLayout 可自定义 背景色/渐变背景/形状/阴影/描边
   

<img src="screenshot.png" width="300" />
 

## 引入 

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:shapeable:0.10.0"
}
```

## 属性

```xml 
<declare-styleable name="ShapeableDrawable">
    <!-- 背景色 -->
    <attr name="backgroundTint" />

    <!-- 描边 -->
    <attr name="strokeColor" />
    <attr name="strokeWidth" />

    <!-- 形状 -->
    <attr name="shapeAppearance" />
    <attr name="shapeAppearanceOverlay" />

    <!-- 角大小：设置四个角的大小，如果只想设置部分角大小，请不要指定此属性 -->
    <attr name="cornerSize" />
    
    <!-- 角类型：设置四个角的类型，如果只想设置部分角类型，请不要指定此属性 -->
    <attr name="cornerType" format="enum">
        <enum name="rounded" value="0" />
        <enum name="cut" value="1" />
        <enum name="concave" value="2" />
    </attr>

    <!-- 阴影 -->
    <attr name="shadowColor" format="color" />
    <attr name="shadowRadius" format="dimension" />
    <attr name="shadowOffsetX" format="dimension" />
    <attr name="shadowOffsetY" format="dimension" />

    <!-- 气泡箭头 -->
    <attr name="arrowSize" format="dimension" />
    <attr name="arrowOffset" format="dimension" />
    <attr name="arrowEdge" format="enum">
        <enum name="left" value="1" />
        <enum name="top" value="2" />
        <enum name="right" value="3" />
        <enum name="bottom" value="4" />
    </attr>
    <attr name="arrowAlign" format="enum">
        <enum name="start" value="1" />
        <enum name="center" value="2" />
        <enum name="end" value="3" />
    </attr>
</declare-styleable>
``` 
## 用法

使用 `ShapeableFrameLayout / ShapeableLinearLayout`
            
```xml
<me.reezy.cosmo.shapeable.ShapeableFrameLayout
    android:layout_width="80dp"
    android:layout_height="40dp"
    android:layout_margin="10dp"
    android:clickable="true"
    android:gravity="center"
    app:cornerSize="5dp"
    app:cornerType="rounded"
    app:shadowColor="#40ff0000"
    app:shadowRadius="10dp"
    app:shapeAppearance="@style/ShapeAppearance.Material3.Corner.Full"
    app:strokeColor="@color/teal_700"
    app:strokeWidth="1dp" />
```

使用 `drawable` 可用于普通 `View`

```xml 
<View
    android:layout_width="200dp"
    android:layout_height="50dp"
    android:layout_columnSpan="3"
    android:layout_gravity="center_horizontal"
    android:layout_margin="10dp"
    android:layout_marginTop="50dp"
    android:background="@drawable/shapeable" />
```

`shapeable.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<me.reezy.cosmo.shapeable.ShapeableDrawable xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    app:arrowAlign="center"
    app:arrowEdge="bottom"
    app:arrowSize="10dp"
    app:backgroundTint="#400000cc"
    app:cornerSize="10dp"
    app:cornerType="concave"
    app:shadowColor="#40ffffff"
    app:shadowRadius="10dp"
    app:strokeColor="@android:color/holo_blue_dark"
    app:strokeWidth="2dp" />
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).