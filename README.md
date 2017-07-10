# AnimCube for Android [ ![Download](https://api.bintray.com/packages/cjurjiu/cjurjiu-opensource/animcube-android/images/download.svg?version=1.0.2) ](https://bintray.com/cjurjiu/cjurjiu-opensource/animcube-android/1.0.2/link) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))
An Android port (with added features) of [Josef Jelinek's AnimCube](http://software.rubikscube.info/AnimCube) Java Web applet.

Animating moves towards a solution | Touch interactions
:---: | :---:
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/animate_moves_forward.gif" width="50%" /> | <img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/free_interaction.gif"  width="50%" />

## Table of contents
  * [Usage](#usage)
  * [Animating moves](#animating-moves)
  * [Custom colors](#custom-colors)
  * [Saving the state](#saving-the-state)
  * [Event listeners](#event-listeners)
  * [Debuggable mode](#debuggable-mode)
  * [Parameters available in XML](#parameters-available-in-xml)
  * [Binaries](#binaries)

## Usage

Add the cube to your view hierarchy:

```xml
<com.catalinjurjiu.animcubeandroid.AnimCube
     android:id="@+id/animcube"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     cube:backFacesDistance="4"/><!--optional if you want the back faces to be displayed-->
```

And you'll get:

No back faces | With back faces
:--: | :--:
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/cube_view_no_backfaces.png" width="50%"/> | <img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/cube_view.png" width="50%"/>

## Animating moves

To start an animation, first you need to define the move sequence. This can be either either through XML:

```xml
<com.catalinjurjiu.animcubeandroid.AnimCube
     android:id="@+id/animcube"
     android:layout_width="match_parent"
     android:layout_height="match_parent"     
     cube:backFacesDistance="4"
     cube:moves="R2' U M U' R2' U M' U'"/><!--defines the sequence of moves to be performed-->
```
Or from code:
```java
AnimCube animCube = findViewById(R.id.animcube);
animCube.setMoveSequence("R2' U M U' R2' U M' U'");
```
Then, tell the cube to perform the specified moves:

```java
animCube.animateMoveSequence();
//or animate it in reverse, from the end and with opposite twisting direction
animCube.animateMoveSequenceReversed(); 
```
Or you can animate only one move at a time:

```java
animCube.animateMove();
//or in reverse
animCube.animateMoveReversed();
```
To stop an animation prematurely, use `stopAnimation`:
```java
animCube.stopAnimation();
```
As a side effect, `stopAnimation` instantly the applies the move it interrupted.

You can also just apply a move sequence, or an individual move, without an animation:

```java
animCube.applyMoveSequence();
//individual move
animCube.applyMove();
```

Both `apply` methods also have `reverse` equivalents.
## Custom colors
If the default colors don't play nicely with your theme's color palette, or if you just don't fancy them, custom ones can be specified through XML.

To do so, just define your 6 custom colors as a XML color array, in `arrays.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="custom_cube_colors">
        <item>#ce3232</item>
        <item>#ffa239</item>
        <item>#737581</item>
        <item>#e7dc00</item>
        <item>#01641c</item>
        <item>#141291</item>
    </array>
</resources>    
```

Then apply them on the cube through XML:

```xml
<com.catalinjurjiu.animcubeandroid.AnimCube
    android:id="@+id/animcube"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    cube:backFacesDistance="4"
    cube:backgroundColor="#3A393A"
    cube:cubeColors="@array/custom_cube_colors"
    cube:faceletsContourColor="#ffffff"/>
```
As it can be seen, the background color & the contour of the facelets can also be customized. This yields:

<p align="center">
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/custom_colors.png" width="30%"/>
</p>

## Saving the state

When a configuration change occurs, the cube knows <i>how</i> to save its state, but it needs to be <i>told</i> to do so. This will likely be change to happen automatically in the future.

<p align="center">
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/screen_rotation.gif" width="30%"/>
</p>

For now however, ensuring the cube saves its state is relatively simple. Just add the following to your Activity/Fragment:

```java
public class MainActivity extends Activity {
    public static final String ANIM_CUBE_SAVE_STATE_BUNDLE_ID = "animCube";
    private AnimCube animCube;
    ...

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(ANIM_CUBE_SAVE_STATE_BUNDLE_ID, animCube.saveState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        animCube.restoreState(savedInstanceState.getBundle(ANIM_CUBE_SAVE_STATE_BUNDLE_ID));
    }
}
```
## Event listeners

### Animation events
To be notified whenever an animation is finished, you can register an `OnCubeAnimationFinishedListener`. This makes `AnimCube` call `onAnimationFinished` every time a call to animate, or apply for one or move moves has finished making its changes.

**Note:** When animating/applying a move sequence (i.e. not individual moves) `onAnimationFinished` is only called when the end of the move sequence is reached, or when `stopAnimation` is called. It is *not* called for every move in the sequence.

### Cube changed events
All the animate/apply calls change the underlying cube model. Additionally, this can also happen when the cube is editable and the user manually twists a layer.

To be notified when the cube model is changed, use an `OnCubeModelUpdatedListener`. This also allows you to be notified when each move is applied, when animating a move sequence, since the `OnCubeAnimationFinishedListener` is only notified when the whole sequence has finished animating. 

**Note:** The set `OnCubeModelUpdatedListener` is also notified for each move in a move sequence, when it is applied with `AnimCube#applyMoveSequence` & `AnimCube#applyMoveSequenceReversed`. This happens because, although to the user the whole move sequence seems to be applied instantly, internally the moves are applied one by one, and rendering occurrs only at the end.

## Debuggable mode

**TL;DR:** Always use `AnimCube.java`, only use `AnimCubeDebuggable.java` when you need detailed logs to file an issue.

**Long version:**

Currently the library contains two classes: `AnimCube.java` & `AnimCubeDebug.java`. In terms of behavior they are equivalent, and generally you should only ever use `AnimCube.java`. Strictly speaking though, when it comes to the debug mode, their behavior differs. 

When debug mode is **on**, `AnimCube.java` prints some warnings, if they happen. With debug mode **off**, the warnings are omitted and nothing else happens.

On the other hand, `AnimCubeDebug.java` prints a plethora of debug & info messages to LogCat when debug mode is **on**, and prints nothing when it's **off**.

The decision to have two different classes was not an easy one. Internally, both classes rely on utility methods to decide whether to print a certain message or not. However, even if in the end logging to logcat doesn't happen, the string message is still allocated. An alternative would've been to check the condition before allocating the string, but then the code itself would be polluted with tons of conditional checks.

By removing all debug & info logs from `AnimCube.java`, memory is not polluted with strings that never get printed when debug mode is off. Yet, all the debug messages can still be obtained if `AnimCube` is swapped with `AnimCubeDebug` when attempting to reproduce an issue.

To turn debug mode on from code, use `AnimCube#setDebuggable(boolean)`. To enable it from XML, use the `debuggable` attribute:
```xml
<com.catalinjurjiu.animcubeandroid.AnimCube
    android:id="@+id/animcube"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    cube:debuggable="true"/>
```        

By default, debug mode is **disabled**.

## Parameters available in XML
Many of the [original parameters](http://software.rubikscube.info/AnimCube/) have been kept with equivalent behavior. However, certain names were changed. 

This section describes just the configuration params supported by AnimCube-Android, however if you are interested in an actual changelog between the list of parameters provided by the original and this version, see the [Changelog from original AnimCube](./changelog_from_animcube_applet.md).

Parameters list:
  * [backgroundColor](#backgroundColor)
  * [cubeColors](#cubeColors)
  * [faceletContourColor](#faceletContourColor)
  * [initialState](#initialState)
  * [moves](#moves)
  * [editable](#editable)
  * [backFacesDistance](#backFacesDistance)
  * [touchSensitivity](#touchSensitivity)
  * [initialRotation](#initialRotation)
  * [perspective](#perspective)
  * [scale](#scale)
  * [singleRotationSpeed](#singleRotationSpeed)
  * [doubleRotationSpeed](#doubleRotationSpeed)
  * [verticalAlign](#verticalAlign)
  * [debuggable](#debuggable)


### <a name="backgroundColor"></a> backgroundColor - color | reference
Specifies the background color of the cube view.

### <a name="cubeColors"></a> cubeColors - reference
Specifies 6 custom colors to be used by the cube, instead of the default colors. Must be an array defined in XML.

### <a name="faceletContourColor"></a> faceletContourColor - color | reference
Specifies the color of the region between cube facelets. 

### <a name="initialState"></a> initialState - string | reference
Specifies the initial state of the cube. Needs to contain an array of exactly 54 color indexes. Valid indexes are in the range [0,5], with each index corresponding to the following default color:
  * 0 - White
  * 1 - Yellow
  * 2 - Orange
  * 3 - Red
  * 4 - Blue
  * 5 - Green

If custom colors are defined, then the indexes will map on the array of custom colors. For example, if in a custom color scheme Black replaces White, then the index 0 would map to Black.

Example value of *initialState* for a solved cube:
```xml
cube:initialState="000000000111111111222222222333333333444444444555555555"
```

By default, the cube is in the state mentioned above as example.

### <a name="moves"></a> moves - string | reference

Sets the sequence of moves that need to be performed (and optionally, animated). Some of the moves affect centers and they can be moved to another layer from the user's point of view. Such movements **do not affect** the notation from the user's point of view. The characters are not fixed to particular centers.

For example, if an "M" is performed and then an "F" is needed, it should affect the front layer seen in the front position and not the bottom layer, where the center that was in the front position is now placed. The chosen way is very familiar to the "corner-starters" (solving the cube starting from the corners).

The sequence is defined in extended Singmaster's notation. The basis for the turns are six letters of the following meaning.

  * U - Up (rotate top layer)
  * D - Down (rotate bottom layer)
  * F - Front (rotate front layer)
  * B - Back (rotate back layer)
  * L - Left (rotate left layer)
  * R - Right (rotate right layer)

The letter case is important here, because the same - but lowercase - letters are used for different moves. Modifiers can be appended to the move character.

  * Separate characters mean turning the corresponding layer 90 degrees clock-wise.
  * Appending apostrophe "'" or digit "3" means turning 90 degrees counter clock-wise.
  * Appending digit "2" means 180 degrees rotation of the corresponding layer (clock-wise).
  * You can use combination "2'" for double counter clock-wise turn. This combination is useful if you want to show the most efficient directions when using finger shortcuts.

There are also some advanced modifiers that are written immediately after the move letter and right before the basic modifiers already defined. The possible modifiers are:

  * m - middle layer turn between the specified layer and the opposite one
  * c - whole-cube turn in the direction of the specified layer
  * s - slice turn; two opposite layers are turned in the same directions ("Rs" is equal to "R L'" or "L' R")
  * a - anti-slice turn; two opposite layers are turned in the opposite directions ("Ra" is equal to "R L" or "L R")
  * t - thick turn; two adjacent layers (the specified one and the adjacent one) are turned simultaneously

The library supports some additional characters to represent specific moves. The center layers can be rotated using the following characters in combination with previous modifiers.

  * E - equator (between U and D layers in the U'/D direction)
  * S - standing (between F and B layers in the F/B' direction)
  * M - middle (between L and R layers in the L/R' direction)

The library also supports turns of the entire cube. This feature can be used to rotate the cube in order to show the cube in the best position for the current situation to watch the move sequence. The available symbols to rotate the cube are shown in the following table (they can be also combined with the modifiers).

  * X - rotate around x-axis (in the same direction as "R" or "L'" is performed)
  * Y - rotate around y-axis (in the same direction as "F" or "B'" is performed)
  * Z - rotate around z-axis (in the same direction as "U" or "D'" is performed)

There is also a possibility to rotate two adjacent layers simultaneously. The notation and meaning is similar to the face-layer rotations, but the letters are in lowercase.

  * u - up (rotate two top layers)
  * d - down (rotate two bottom layers)
  * f - front (rotate two front layers)
  * b - back (rotate two back layers)
  * l - left (rotate two left layers)
  * r - right (rotate two right layers)

There is yet another character to be used in the parameter value - the dot '.' character. When a dot is found in the sequence during playing the animation, it is delayed for a half of the time the quarter turn is performed.

**Important:** In Josef Jelink's original AnimCube applet there could be several move sequences specified in the same string. The sequences were separated by the semicolon character ';'. This feature however is disabled in this version.<br>
If the move sequence string passed to this method has more than one move sequences defined, only the first will be taken into consideration, and the next will be ignored.

**Note:** For additional details and a few left out alternatives to certain notations, see Josef's complete documentation for the move sequence <a href="http://software.rubikscube.info/AnimCube/#move">here.</a>

### <a name="editable"></a> editable - boolean | reference

If *enabled*, allows the user to modify the cube model through touch events, by rotating faces. If *disabled*, drag events will always rotate the whole cube.  

### <a name="backFacesDistance"></a> backFacesDistance - integer | reference

Controls whether sides pointing away from the user are rendered behind the cube. This parameter sets their distance from the cube. 

Typically, a value smaller than 2 means they won't be visible (hence, disabled). A value too large means they will be rendered outside the screen. 

Usually values between 2 - 10 are good picks, with 0 when they don't need to be displayed.

### <a name="touchSensitivity"></a> touchSensitivity - float | reference

Controls how well the cube reacts to touch events. Expects a **float** in the interval [0f,2f].

Default value is 1f.

### <a name="initialRotation"></a> initialRotation - string | reference

Defines the initial rotation of the cube.

The value can be of any length and can contain characters: 'u', 'd', 'f', 'b', 'l' and 'r' in upper or lower case. The rotation steep is 15 degrees. The default value is "lluu". The rotation axis and direction is similar to rotation of layers, etc.

### <a name="perspective"></a> perspective - integer | reference

This parameter allows to customize the perspective deformation of the cube. The value should consist only of decimal digits. The higher value the closer to a parallel view. 

The default value is 2.

### <a name="scale"></a> scale - integer | reference

This parameter allows to customize the size of the cube. The value should consist only of decimal digits. The higher value the smaller cube. The exact size is computed as 1 / (1 + scale / 10). 

The default value is 0 that causes the cube to fit in window. 

The parameter is useful in combination with *verticalAlign*.

### <a name="verticalAlign"></a> verticalAlign - integer | reference

This parameter allows to position the cube vertically. 

The only permitted values are *"top"*, *"center"* and *"bottom"* for bottom align. 

The default value is 1.

**Note:** The parameter makes sense in combination with scale. With the default scale, the cube will always be centered.

### <a name="singleRotationSpeed"></a> singleRotationSpeed - integer | reference

Sets the rotation speed of a single rotation. This parameter allows to customize the speed of quarter turn separately from face turns. The value should consist only of decimal digits.

The higher value the slower is the animation. The default value is 10, which corresponds to approximately 1 second for face turn and approximately 2/3 seconds for quarter turn if not specified differently.

The face turn speed can be adjusted separately by *doubleRotationSpeed*.

### <a name="doubleRotationSpeed"></a> doubleRotationSpeed - integer | reference

Sets the rotation speed of a double rotation. This parameter allows to customize the speed of face turns separately from quarter turns. The value should consist only of decimal digits.

The higher value the slower is the animation. The default value is 10, which corresponds to approximately 1 second
for the face turn.

The default is set to the 150% of the value of speed.

The quarter turn speed can be adjusted by *singleRotationSpeed*.

### <a name="debuggable"></a> debuggable - boolean | reference

Enables or disables debug mode.

This is disabled by default.

## Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found on [jcenter](https://bintray.com/cjurjiu/cjurjiu-opensource/animcube-android).

Example for Gradle:

```groovy
compile 'com.catalinjurjiu:animcube-android:1.0.2'
```

and for Maven:

```xml
<dependency>
  <groupId>com.catalinjurjiu</groupId>
  <artifactId>animcube-android</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```
and for Ivy:

```xml
<dependency org='com.catalinjurjiu' name='animcube-android' rev='1.0.2'>
  <artifact name='animcube-android' ext='pom' ></artifact>
</dependency>
```
