package com.megaphone.skoozi;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.animation.TypeEvaluator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple Path object that holds information about the points along
 * a path. The API allows you to specify a move location (which essentially
 * jumps from the previous point in the path to the new one), a line location
 * (which creates a line segment from the previous location) and a curve
 * location (which creates a cubic B?zier curve from the previous location).
 */
public class CurvedAnimatorPath {

    // The points in the path
    ArrayList<PathPoint> mPoints = new ArrayList<PathPoint>();


    /**
     * Move from the current path point to the new one
     * specified by x and y. This will create a discontinuity if this point is
     * neither the first point in the path nor the same as the previous point
     * in the path.
     */
    public void moveTo(float x, float y) {
        mPoints.add(PathPoint.moveTo(x, y));
    }

    /**
     * Create a straight line from the current path point to the new one
     * specified by x and y.
     */
    public void lineTo(float x, float y) {
        mPoints.add(PathPoint.lineTo(x, y));
    }

    /**
     * Create a cubic B?zier curve from the current path point to the new one
     * specified by x and y. The curve uses the current path location as the first anchor
     * point, the control points (c0X, c0Y) and (c1X, c1Y), and (x, y) as the end anchor point.
     */
    public void curveTo(float c0X, float c0Y, float c1X, float c1Y, float x, float y) {
        mPoints.add(PathPoint.curveTo(c0X, c0Y, c1X, c1Y, x, y));
    }

    /**
     * Returns a Collection of PathPoint objects that describe all points in the path.
     */
    public Collection<PathPoint> getPoints() {
        return mPoints;
    }

    /**
     * A class that holds information about a location and how the path should get to that
     * location from the previous path location (if any). Any PathPoint holds the information for
     * its location as well as the instructions on how to traverse the preceding interval from the
     * previous location.
     */
    public static class PathPoint {

        /**
         * The possible path operations that describe how to move from a preceding PathPoint to the
         * location described by this PathPoint.
         */
        public static final int MOVE = 0;
        public static final int LINE = 1;
        public static final int CURVE = 2;

        /**
         * The location of this PathPoint
         */
        float mX, mY;

        /**
         * The first control point, if any, for a PathPoint of type CURVE
         */
        float mControl0X, mControl0Y;

        /**
         * The second control point, if any, for a PathPoint of type CURVE
         */
        float mControl1X, mControl1Y;

        /**
         * The motion described by the path to get from the previous PathPoint in an AnimatorPath
         * to the location of this PathPoint. This can be one of MOVE, LINE, or CURVE.
         */
        int mOperation;



        /**
         * Line/Move constructor
         */
        private PathPoint(int operation, float x, float y) {
            mOperation = operation;
            mX = x;
            mY = y;
        }

        /**
         * Curve constructor
         */
        private PathPoint(float c0X, float c0Y, float c1X, float c1Y, float x, float y) {
            mControl0X = c0X;
            mControl0Y = c0Y;
            mControl1X = c1X;
            mControl1Y = c1Y;
            mX = x;
            mY = y;
            mOperation = CURVE;
        }

        /**
         * Constructs and returns a PathPoint object that describes a line to the given xy location.
         */
        public static PathPoint lineTo(float x, float y) {
            return new PathPoint(LINE, x, y);
        }

        /**
         * Constructs and returns a PathPoint object that describes a cubic B?zier curve to the
         * given xy location with the control points at c0 and c1.
         */
        public static PathPoint curveTo(float c0X, float c0Y, float c1X, float c1Y, float x, float y) {
            return new PathPoint(c0X,  c0Y, c1X, c1Y, x, y);
        }

        /**
         * Constructs and returns a PathPoint object that describes a discontinuous move to the given
         * xy location.
         */
        public static PathPoint moveTo(float x, float y) {
            return new PathPoint(MOVE, x, y);
        }
    }

    /**
     * This evaluator interpolates between two PathPoint values given the value t, the
     * proportion traveled between those points. The value of the interpolation depends
     * on the operation specified by the endValue (the operation for the interval between
     * PathPoints is always specified by the end point of that interval).
     */
    public class PathEvaluator implements TypeEvaluator<PathPoint> {
        @Override
        public PathPoint evaluate(float t, PathPoint startValue, PathPoint endValue) {
            float x, y;
            if (endValue.mOperation == PathPoint.CURVE) {
                float oneMinusT = 1 - t;
                x = oneMinusT * oneMinusT * oneMinusT * startValue.mX +
                        3 * oneMinusT * oneMinusT * t * endValue.mControl0X +
                        3 * oneMinusT * t * t * endValue.mControl1X +
                        t * t * t * endValue.mX;
                y = oneMinusT * oneMinusT * oneMinusT * startValue.mY +
                        3 * oneMinusT * oneMinusT * t * endValue.mControl0Y +
                        3 * oneMinusT * t * t * endValue.mControl1Y +
                        t * t * t * endValue.mY;
            } else if (endValue.mOperation == PathPoint.LINE) {
                x = startValue.mX + t * (endValue.mX - startValue.mX);
                y = startValue.mY + t * (endValue.mY - startValue.mY);
            } else {
                x = endValue.mX;
                y = endValue.mY;
            }
            return PathPoint.moveTo(x, y);
        }
    }
}