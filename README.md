# AnimCube for Android [ ![Download](https://api.bintray.com/packages/cjurjiu/cjurjiu-opensource/animcube-android/images/download.svg?version=1.0.2) ](https://bintray.com/cjurjiu/cjurjiu-opensource/animcube-android/1.0.2/link) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))
An Android port (with added features) of Josef Jelinek's original AnimCube

Animating moves towards a solution | Touch interactions
:---: | :---:
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/animate_moves_forward.gif" width="50%" /> | <img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/free_interaction.gif"  width="50%" />


## Usage

Add the cube to your view hierachy:

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
Then, tell the cube to perform the specified moves sequence:

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
To stop an animation prematurelly, use `stopAnimation`:
```java
animCube.stopAnimation();
```
As a side effect, `stopAnimation` instantly the applies the move it interrupted.

You can also just apply a move sequence, without animating the moves:

```java
animCube.applyMoveSequence();
//in reverse
animCube.applyMoveSequenceReversed();
```
Individual moves can also be applied with no animation:

```java
animCube.applyMove();
animCube.applyMoveReversed();
```
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
</resources.    
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

When a configuration change occurrs, the cube knows <i>how</i> to save its state, but it needs to be <i>told</i> to do so. This will likely be change to happen automatically in the future.

<p align="center">
<img src="https://github.com/cjurjiu/animcubeandroid/blob/master/github_media/screen_rotation.gif" width="30%"/>
</p>

For now however, ensuring the cube saves its state is relativelly simple. Just add the following to your Activity/Fragment:

```java
public class MainActivity extends Activity
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
## Being notified of events

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

The decission to have two different classes wasn't an easy one. Internally, both classes rely on utility methods to decide whether to print a certain message or not. However, even if in the end logging to logcat doesn't happen, the string message is still allocated. An alternative would've been to check the condition before allocating the string, but then the code itself would be polluted with tons of conditional checks.

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

## Parameters available in XML
Many of the [original parameters](http://software.rubikscube.info/AnimCube/) have been kept with equivalent behavior. However, certain names were changed. 

This section describes just the configuration params supported by AnimCube-Android, however if you are interested in an actual changelog between the list of parameters provided by the original and this version, see [CHANGELOG_FROM_ANIMCUBE_JELINEK.md](https://github.com/cjurjiu/animcubeandroid)

//TODO
