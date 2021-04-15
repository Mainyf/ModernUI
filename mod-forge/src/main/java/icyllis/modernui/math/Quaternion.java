/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.math;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Quaternion, mostly used for rotations.
 */
@SuppressWarnings("unused")
public class Quaternion implements Cloneable {

    private float x;
    private float y;
    private float z;
    private float w;

    /**
     * Create a zero quaternion.
     */
    public Quaternion() {
    }

    /**
     * Create a quaternion with given components.
     *
     * @param x the x-component of the quaternion
     * @param y the y-component of the quaternion
     * @param z the z-component of the quaternion
     * @param w the w-component of the quaternion
     */
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Create a copy of {@code q} if it's not null, or a identity quaternion
     * otherwise.
     *
     * @param q the quaternion to copy from
     * @return a copy of the quaternion
     */
    @Nonnull
    public static Quaternion copy(@Nullable Quaternion q) {
        return q == null ? identity() : q.clone();
    }

    /**
     * Create a new identity quaternion.
     *
     * @return an identity quaternion
     */
    @Nonnull
    public static Quaternion identity() {
        Quaternion q = new Quaternion();
        q.w = 1.0f;
        return q;
    }

    /**
     * Create a quaternion from the given Euler rotation angles in radians.
     * The returned quaternion will be a normalized rotational quaternion.
     *
     * @param rotationX the Euler pitch angle in radians. (rotation about the X axis)
     * @param rotationY the Euler yaw angle in radians. (rotation about the Y axis)
     * @param rotationZ the Euler roll angle in radians. (rotation about the Z axis)
     * @return the resulting quaternion
     */
    @Nonnull
    public static Quaternion fromEulerAngles(float rotationX, float rotationY, float rotationZ) {
        Quaternion q = new Quaternion();
        q.setFromEulerAngles(rotationX, rotationY, rotationZ);
        return q;
    }

    /**
     * Create a quaternion from the given axis and angle. The axis must be
     * a normalized (unit) vector.
     *
     * @param axisX x-coordinate of rotation axis
     * @param axisY y-coordinate of rotation axis
     * @param axisZ z-coordinate of rotation axis
     * @param angle rotation angle in radians
     * @return the resulting quaternion
     */
    @Nonnull
    public static Quaternion fromAxisAngle(float axisX, float axisY, float axisZ, float angle) {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(axisX, axisY, axisZ, angle);
        return q;
    }

    /**
     * Set all values of this quaternion using the given components.
     *
     * @param x the x-component to set
     * @param y the y-component to set
     * @param z the z-component to set
     * @param w the w-component to set
     */
    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Set all values of this quaternion using the given quaternion.
     *
     * @param q the quaternion to copy from
     */
    public void set(@Nonnull Quaternion q) {
        x = q.x;
        y = q.y;
        z = q.z;
        w = q.w;
    }

    /**
     * Add a quaternion to this quaternion.
     *
     * @param q the quaternion to add
     */
    public void add(@Nonnull Quaternion q) {
        w += q.w;
        x += q.x;
        y += q.y;
        z += q.z;
    }

    /**
     * Subtract a quaternion from this quaternion.
     *
     * @param q the quaternion to subtract
     */
    public void subtract(@Nonnull Quaternion q) {
        w -= q.w;
        x -= q.x;
        y -= q.y;
        z -= q.z;
    }

    /**
     * Multiply this quaternion by a factor.
     *
     * @param s the factor to multiply.
     */
    public void multiply(float s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
    }

    /**
     * Multiply this quaternion by some other quaternion.
     *
     * @param q the quaternion to multiply with
     */
    public void multiply(@Nonnull Quaternion q) {
        set(w * q.x + x * q.w + y * q.z - z * q.y,
                w * q.y - x * q.z + y * q.w + z * q.x,
                w * q.z + x * q.y - y * q.x + z * q.w,
                w * q.w - x * q.x - y * q.y - z * q.z);
    }

    /**
     * Calculate the magnitude of this quaternion, namely the 2-norm
     * (euclidean norm) or the length.
     *
     * @return the magnitude of this quaternion
     */
    public float length() {
        return MathUtil.sqrt(w * w + x * x + y * y + z * z);
    }

    public float lengthSquared() {
        return w * w + x * x + y * y + z * z;
    }

    /**
     * Calculate the dot product of this quaternion with the given x, y, z and w components.
     */
    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    /**
     * Calculate the dot product of this quaternion with the given quaternion.
     *
     * @param q the quaternion to dot product with.
     */
    public float dot(@Nonnull Quaternion q) {
        return x * q.x + y * q.y + z * q.z + w * q.w;
    }

    /**
     * Calculate the inverse of this quaternion. If rotational, it will produce
     * a the inverse rotation.
     */
    public void inverse() {
        final float sq = lengthSquared();
        if (MathUtil.approxEqual(sq, 1.0f)) {
            // equal to invert w component
            conjugate();
        } else {
            final float invSq = 1.0f / sq;
            w *= invSq;
            x = -x * invSq;
            y = -y * invSq;
            z = -z * invSq;
        }
    }

    /**
     * Returns whether this quaternion is normalized.
     *
     * @return {@code true} if normalized
     */
    public boolean isNormalized() {
        return MathUtil.approxEqual(lengthSquared(), 1.0f);
    }

    /**
     * Normalize this quaternion to unit length.
     */
    public void normalize() {
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            setIdentity();
        } else {
            final float invNorm = 1.0f / MathUtil.sqrt(sq);
            x *= invNorm;
            y *= invNorm;
            z *= invNorm;
            w *= invNorm;
        }
    }

    /**
     * Normalize this quaternion to approximately unit length.
     */
    public void normalizeFast() {
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            setIdentity();
        } else {
            final float invNorm = MathUtil.fastInvSqrt(sq);
            x *= invNorm;
            y *= invNorm;
            z *= invNorm;
            w *= invNorm;
        }
    }

    /**
     * Set this quaternion to the zero quaternion.
     */
    public void setZero() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        w = 0.0f;
    }

    /**
     * Calculate whether this quaternion is approximately equivalent to a identity quaternion.
     *
     * @return {@code true} if this quaternion is identity.
     */
    public boolean isIdentity() {
        return MathUtil.approxZero(x, y, z) && MathUtil.approxEqual(w, 1.0f);
    }

    /**
     * Set this quaternion to the identity quaternion.
     */
    public void setIdentity() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        w = 1.0f;
    }

    /**
     * Conjugate this quaternion <code>[-x, -y, -z, w]</code>.
     */
    public void conjugate() {
        x = -x;
        y = -y;
        z = -z;
    }

    /**
     * Set this quaternion to a spherical linear interpolation between two quaternions
     * by the given factor. The two quaternions should be rotational so they should
     * be normalized. This method does not normalize this quaternion.
     *
     * @param a the start quaternion
     * @param b the end quaternion
     * @param t a value between 0.0 and 1.0 representing interpolation
     */
    public void slerp(@Nonnull Quaternion a, @Nonnull Quaternion b, float t) {
        if (t <= 0.0f) {
            set(a);
        } else if (t >= 1.0f) {
            set(b);
        } else if (MathUtil.approxZero(a.lengthSquared())) {
            if (MathUtil.approxZero(b.lengthSquared())) {
                setIdentity();
            } else {
                set(b);
            }
        } else if (MathUtil.approxZero(b.lengthSquared())) {
            set(a);
        } else {
            float cosHalfAngle = a.dot(b);
            final float s;
            if (cosHalfAngle >= 0.95f) {
                // quaternions are close to the same rotation, linear interpolation is used
                s = 1.0f - t;
            } else if (cosHalfAngle <= -0.99f) {
                // the quaternions are nearly opposite, we can pick any axis normal to a,b to do the rotation
                s = t = 0.5f;
            } else {
                boolean negative = false;
                if (cosHalfAngle <= -1.0e-6f) {
                    cosHalfAngle = -cosHalfAngle;
                    negative = true;
                }

                final float halfAngle = MathUtil.acos(cosHalfAngle);
                float sinHalfAngle = MathUtil.sin(halfAngle);
                if (Math.abs(sinHalfAngle) < 0.001f) {
                    // possible?
                    s = t = 0.5f;
                } else {
                    sinHalfAngle = 1.0f / sinHalfAngle;
                    s = MathUtil.sin((1.0f - t) * halfAngle) * sinHalfAngle;
                    t = MathUtil.sin(t * halfAngle) * sinHalfAngle;
                    if (negative)
                        t = -t;
                }
            }

            x = a.x * s + b.x * t;
            y = a.y * s + b.y * t;
            z = a.z * s + b.z * t;
            w = a.w * s + b.w * t;
        }
    }

    /**
     * Rotate this quaternion by the given axis and angle. The axis must be
     * a normalized (unit) vector.
     *
     * @param axisX x-coordinate of rotation axis
     * @param axisY y-coordinate of rotation axis
     * @param axisZ z-coordinate of rotation axis
     * @param angle rotation angle in radians
     */
    public void rotateByAxis(float axisX, float axisY, float axisZ, float angle) {
        if (angle == 0.0f)
            return;
        angle *= 0.5f;
        final float sin = MathUtil.sin(angle);
        final float qx = axisX * sin;
        final float qy = axisY * sin;
        final float qz = axisZ * sin;
        final float qw = MathUtil.cos(angle);
        set(x * qw + y * qz - z * qy + w * qx,
                -x * qz + y * qw + z * qx + w * qy,
                x * qy - y * qx + z * qw + w * qz,
                -x * qx - y * qy - z * qz + w * qw);
    }

    /**
     * Rotate this quaternion around X axis with the given angle in radians.
     *
     * @param angle rotation angle in radians
     */
    public void rotateX(float angle) {
        if (angle == 0.0f)
            return;
        angle *= 0.5f;
        final float sin = MathUtil.sin(angle);
        final float cos = MathUtil.cos(angle);
        set(x * cos + w * sin,
                y * cos + z * sin,
                -y * sin + z * cos,
                -x * sin + w * cos);
    }

    /**
     * Rotate this quaternion around Y axis with the given angle in radians.
     *
     * @param angle rotation angle in radians
     */
    public void rotateY(float angle) {
        if (angle == 0.0f)
            return;
        angle *= 0.5f;
        final float sin = MathUtil.sin(angle);
        final float cos = MathUtil.cos(angle);
        set(x * cos - z * sin,
                y * cos + w * sin,
                x * sin + z * cos,
                -y * sin + w * cos);
    }

    /**
     * Rotate this quaternion around Z axis with the given angle in radians.
     *
     * @param angle rotation angle in radians
     */
    public void rotateZ(float angle) {
        if (angle == 0.0f)
            return;
        angle *= 0.5f;
        final float sin = MathUtil.sin(angle);
        final float cos = MathUtil.cos(angle);
        set(x * cos + y * sin,
                -x * sin + y * cos,
                z * cos + w * sin,
                -z * sin + w * cos);
    }

    /**
     * Rotates this quaternion from the given Euler rotation angles in radians.
     * <p>
     * The rotations are applied in the given order and using chained rotation per axis:
     * <ul>
     *  <li>x - pitch - {@link #rotateX(float)}</li>
     *  <li>y - yaw   - {@link #rotateY(float)}</li>
     *  <li>z - roll  - {@link #rotateZ(float)}</li>
     * </ul>
     * </p>
     *
     * @param rotationX the Euler pitch angle in radians. (rotation about the X axis)
     * @param rotationY the Euler yaw angle in radians. (rotation about the Y axis)
     * @param rotationZ the Euler roll angle in radians. (rotation about the Z axis)
     * @see #rotateY(float)
     * @see #rotateZ(float)
     * @see #rotateX(float)
     */
    public void rotateByEuler(float rotationX, float rotationY, float rotationZ) {
        rotateX(rotationX);
        rotateY(rotationY);
        rotateZ(rotationZ);
    }

    /**
     * Set this quaternion components from the given axis and angle. The axis must be
     * a normalized (unit) vector.
     *
     * @param axisX x-coordinate of rotation axis
     * @param axisY y-coordinate of rotation axis
     * @param axisZ z-coordinate of rotation axis
     * @param angle rotation angle in radians
     */
    public void setFromAxisAngle(float axisX, float axisY, float axisZ, float angle) {
        if (MathUtil.approxZero(axisX, axisY, axisZ))
            setIdentity();
        else {
            angle *= 0.5f;
            final float sin = MathUtil.sin(angle);
            x = axisX * sin;
            y = axisY * sin;
            z = axisZ * sin;
            w = MathUtil.cos(angle);
        }
    }

    /**
     * Set this quaternion components from the given Euler rotation angles in radians.
     *
     * @param rotationX the Euler pitch angle in radians. (rotation about the X axis)
     * @param rotationY the Euler yaw angle in radians. (rotation about the Y axis)
     * @param rotationZ the Euler roll angle in radians. (rotation about the Z axis)
     */
    public void setFromEulerAngles(float rotationX, float rotationY, float rotationZ) {
        if (MathUtil.approxZero(rotationX, rotationY, rotationZ))
            setIdentity();
        else {
            rotationX *= 0.5f;
            rotationY *= 0.5f;
            rotationZ *= 0.5f;

            final float sx = MathUtil.sin(rotationX);
            final float cx = MathUtil.cos(rotationX);
            final float sy = MathUtil.sin(rotationY);
            final float cy = MathUtil.cos(rotationY);
            final float sz = MathUtil.sin(rotationZ);
            final float cz = MathUtil.cos(rotationZ);

            final float cc = cy * cz;
            final float ss = sy * sz;
            final float cs = cy * sz;
            final float sc = sy * cz;

            w = cc * cx - ss * sx;
            x = cc * sx + ss * cx;
            y = sc * cx + cs * sx;
            z = cs * cx - sc * sx;
        }
    }

    /**
     * Transform the rotational quaternion to axis based rotation angles.
     *
     * @param axis the array for storing the axis coordinates.
     * @return the rotation angle in radians.
     */
    public float toAxisAngle(@Nonnull float[] axis) {
        if (axis.length < 3)
            throw new IllegalArgumentException("The array length must be at least 3");
        float l = x * x + y * y + z * z;
        if (MathUtil.approxZero(l)) {
            axis[0] = 1.0f;
            axis[1] = 0.0f;
            axis[2] = 0.0f;
            return 0.0f;
        }
        l = 1.0f / MathUtil.sqrt(l);
        axis[0] = x * l;
        axis[1] = y * l;
        axis[2] = z * l;
        return MathUtil.acos(w) * 2.0f;
    }

    /**
     * Transform this quaternion to Euler rotation angles in radians (pitchX, yawY and rollZ).
     *
     * @param result the array for storing the result.
     */
    public void toEulerAngles(@Nonnull float[] result) {
        if (result.length < 3)
            throw new IllegalArgumentException("The array length must be at least 3");
        final float sqx = x * x;
        final float sqy = y * y;
        final float sqz = z * z;
        final float sqw = w * w;
        // if normalized is one
        final float sq = sqx + sqy + sqz + sqw;
        final float f = x * y + z * w;

        if (f > 0.499f * sq) {
            result[0] = 0.0f;
            result[1] = 2.0f * MathUtil.atan2(x, w);
            result[2] = MathUtil.PI_OVER_2;
        } else if (f < -0.499f * sq) {
            result[0] = 0.0f;
            result[1] = -2.0f * MathUtil.atan2(x, w);
            result[2] = -MathUtil.PI_OVER_2;
        } else {
            result[0] = MathUtil.atan2(2.0f * (x * w - y * z), -sqx + sqy - sqz + sqw);
            result[1] = MathUtil.atan2(2.0f * (y * w - x * z), sqx - sqy - sqz + sqw);
            result[2] = MathUtil.asin(2.0f * f / sq);
        }
    }

    /**
     * Transform this quaternion to a normalized 3x3 column matrix representing
     * the rotation.
     *
     * @return the resulting matrix
     */
    @Nonnull
    public Matrix3 toMatrix3() {
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            return Matrix3.identity();
        }
        final float is;
        if (MathUtil.approxEqual(sq, 1.0f)) {
            is = 2.0f;
        } else {
            is = 2.0f / sq;
        }
        Matrix3 mat = new Matrix3();
        final float xs = is * x;
        final float ys = is * y;
        final float zs = is * z;

        final float xx = x * xs;
        final float xy = x * ys;
        final float xz = x * zs;
        final float xw = xs * w;
        final float yy = y * ys;
        final float yz = y * zs;
        final float yw = ys * w;
        final float zz = z * zs;
        final float zw = zs * w;

        mat.m11 = 1.0f - (yy + zz);
        mat.m22 = 1.0f - (xx + zz);
        mat.m33 = 1.0f - (xx + yy);

        mat.m21 = xy - zw;
        mat.m31 = xz + yw;
        mat.m12 = xy + zw;
        mat.m32 = yz - xw;
        mat.m13 = xz - yw;
        mat.m23 = yz + xw;
        return mat;
    }

    /**
     * Transform this quaternion to a normalized 4x4 column matrix representing
     * the rotation.
     *
     * @return the resulting matrix
     */
    @Nonnull
    public Matrix4 toMatrix4() {
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            return Matrix4.identity();
        }
        final float is;
        if (MathUtil.approxEqual(sq, 1.0f)) {
            is = 2.0f;
        } else {
            is = 2.0f / sq;
        }
        Matrix4 mat = new Matrix4();
        final float xs = is * x;
        final float ys = is * y;
        final float zs = is * z;

        final float xx = x * xs;
        final float xy = x * ys;
        final float xz = x * zs;
        final float xw = xs * w;
        final float yy = y * ys;
        final float yz = y * zs;
        final float yw = ys * w;
        final float zz = z * zs;
        final float zw = zs * w;

        mat.m11 = 1.0f - (yy + zz);
        mat.m22 = 1.0f - (xx + zz);
        mat.m33 = 1.0f - (xx + yy);

        mat.m21 = xy - zw;
        mat.m31 = xz + yw;
        mat.m12 = xy + zw;
        mat.m32 = yz - xw;
        mat.m13 = xz - yw;
        mat.m23 = yz + xw;

        mat.m44 = 1.0f;
        return mat;
    }

    /**
     * Transform this quaternion to a normalized 3x3 column matrix representing
     * the rotation. If recycle matrix is null, a new matrix will be returned.
     *
     * @param recycle a matrix for storing result if you want to recycle it
     * @return the resulting matrix
     */
    @Nonnull
    public Matrix3 toMatrix(@Nullable Matrix3 recycle) {
        if (recycle == null)
            return toMatrix3();
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            recycle.setIdentity();
            return recycle;
        }
        final float inv;
        if (MathUtil.approxEqual(sq, 1.0f)) {
            inv = 2.0f;
        } else {
            inv = 2.0f / sq;
        }
        final float xs = inv * x;
        final float ys = inv * y;
        final float zs = inv * z;

        final float xx = x * xs;
        final float xy = x * ys;
        final float xz = x * zs;
        final float xw = xs * w;
        final float yy = y * ys;
        final float yz = y * zs;
        final float yw = ys * w;
        final float zz = z * zs;
        final float zw = zs * w;

        recycle.m11 = 1.0f - (yy + zz);
        recycle.m21 = xy - zw;
        recycle.m31 = xz + yw;

        recycle.m12 = xy + zw;
        recycle.m22 = 1.0f - (xx + zz);
        recycle.m32 = yz - xw;

        recycle.m13 = xz - yw;
        recycle.m23 = yz + xw;
        recycle.m33 = 1.0f - (xx + yy);
        return recycle;
    }

    /**
     * Transform this quaternion to a normalized 4x4 column matrix representing
     * the rotation. If recycle matrix is null, a new matrix will be returned.
     *
     * @param recycle a matrix for storing result if you want to recycle it
     * @return the resulting matrix
     */
    @Nonnull
    public Matrix4 toMatrix(@Nullable Matrix4 recycle) {
        if (recycle == null)
            return toMatrix4();
        final float sq = lengthSquared();
        if (sq < 1.0e-6f) {
            recycle.setIdentity();
            return recycle;
        }
        final float inv;
        if (MathUtil.approxEqual(sq, 1.0f)) {
            inv = 2.0f;
        } else {
            inv = 2.0f / sq;
        }
        final float xs = inv * x;
        final float ys = inv * y;
        final float zs = inv * z;

        final float xx = x * xs;
        final float xy = x * ys;
        final float xz = x * zs;
        final float xw = xs * w;
        final float yy = y * ys;
        final float yz = y * zs;
        final float yw = ys * w;
        final float zz = z * zs;
        final float zw = zs * w;

        recycle.m11 = 1.0f - (yy + zz);
        recycle.m21 = xy - zw;
        recycle.m31 = xz + yw;
        recycle.m41 = 0.0f;

        recycle.m12 = xy + zw;
        recycle.m22 = 1.0f - (xx + zz);
        recycle.m32 = yz - xw;
        recycle.m42 = 0.0f;

        recycle.m13 = xz - yw;
        recycle.m23 = yz + xw;
        recycle.m33 = 1.0f - (xx + yy);
        recycle.m43 = 0.0f;

        recycle.m14 = 0.0f;
        recycle.m24 = 0.0f;
        recycle.m34 = 0.0f;
        recycle.m44 = 1.0f;
        return recycle;
    }

    /**
     * Returns whether this quaternion is approximately equivalent to given quaternion.
     *
     * @param q the quaternion to compare.
     * @return {@code true} if this matrix is equivalent to other.
     */
    public boolean isEqual(@Nonnull Quaternion q) {
        if (this == q) return true;
        return MathUtil.approxEqual(x, q.x) &&
                MathUtil.approxEqual(y, q.y) &&
                MathUtil.approxEqual(z, q.z) &&
                MathUtil.approxEqual(w, q.w);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quaternion that = (Quaternion) o;

        if (!MathUtil.exactlyEqual(that.x, x)) return false;
        if (!MathUtil.exactlyEqual(that.y, y)) return false;
        if (!MathUtil.exactlyEqual(that.z, z)) return false;
        return MathUtil.exactlyEqual(that.w, w);
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (w != +0.0f ? Float.floatToIntBits(w) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Quaternion{" + "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }

    @Override
    public Quaternion clone() {
        try {
            return (Quaternion) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
