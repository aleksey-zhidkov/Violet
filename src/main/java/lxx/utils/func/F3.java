package lxx.utils.func;

import lxx.movement.orbital.OrbitDirection;

public interface F3<ARG1, ARG2, ARG3, RETURN> {

    RETURN f(ARG1 arg1, ARG2 arg2, ARG3 arg3, OrbitDirection dir);

}
