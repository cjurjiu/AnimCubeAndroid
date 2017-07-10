
# Parameters comparison to the original AnimCube

| AnimCube Original| AnimCube Android       |
| :--------------- | :--------------------- |
| config           | -                      |
| bgcolor          | backgroundColor        |
| butbgcolor       | -                      |
| colorscheme      | -                      |
| colors           | -                      |
| position         | initialRotation        |
| speed            | singleRotationSpeed    |
| doublespeed      | doubleRotationSpeed    |
| perspective      | perspective            |
| scale            | scale                  |
| align            | verticalAlign          |
| hint             | backFacesDistance      |
| buttonbar        | -                      |
| edit             | editable               |
| movetext         | -                      |
| fonttype         | -                      |
| metric           | -                      |
| move             | moves                  |
| initmove         | -                      |
| initmove         | -                      |
| initrevmove      | -                      |
| initrevmove      | -                      |
| demo             | -                      |
| demo             | -                      |
| facelets         | initialState \*        |
| pos              | -                      |
| -                | cubeColors             | 
| -                | faceletsContourColor   |
| -                | touchSensitivity       |
| -                | debuggable             |

\* Strictly speaking, not a complete match, but both have the same role.

When porting `AnimCube` to Android, certain things have been removed since they didn't make sense when it comes to Android. 

Additionally, everything related to buttons & text display has also been removed, as these should be implemented through native Android widgets, and should be project specific.

To allow users of the library to always know what the cube is displaying, the `OnCubeModelUpdatedListener` & `OnCubeAnimationFinishedListener` classes have been added.