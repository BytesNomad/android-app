package net.ivpn.client.v2.map.animation

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import net.ivpn.client.rest.data.model.ServerLocation
import net.ivpn.client.v2.map.MapView
import net.ivpn.client.v2.map.dialogue.DialogueDrawer

class MapAnimator(val listener: AnimatorListener) {

    var waveAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var appearProgress = 0f
    private var movementProgress = 0f
    private var waveProgress = 0f

    var animationState = AnimationState.NONE
    var isWavesEnabled = false

    fun centerGateway(
            startX: Float,
            startY: Float,
            locations: ArrayList<ServerLocation>,
            dialogState: DialogueDrawer.DialogState
    ) {
        animationState = AnimationState.MOVEMENT
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.CENTER_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                animationState = AnimationState.NONE
                listener.onCenterAnimationFinish(locations, dialogState)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.updateCenterGatewayProgress(movementProgress, startX, startY, locations.first())
        }
        movementAnimator.start()
    }

    fun centerLocation(
            startX: Float,
            startY: Float,
            dialogState: DialogueDrawer.DialogState,
            animationType: MovementAnimationType) {
        animationState = AnimationState.MOVEMENT
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.CENTER_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                animationState = AnimationState.NONE
                listener.onCenterAnimationFinish(null, dialogState)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.updateMovementProgress(movementProgress, startX, startY, animationType)
        }
        movementAnimator.start()
    }

    fun startMovementAnimation(startX: Float, startY: Float) {
        animationState = AnimationState.MOVEMENT
        this.startX = startX
        this.startY = startY
        val movementAnimator = ValueAnimator.ofFloat(0f, 1f)
        movementAnimator.duration = MapView.MOVEMENT_ANIMATION_DURATION
        movementAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                startAppearAnimation()
                listener.onEndMovementAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                isWavesEnabled = false
                if (waveAnimator.isRunning) {
                    waveAnimator.cancel()
                }
                listener.onStartMovementAnimation()
            }

        })
        movementAnimator.addUpdateListener { valueAnimator ->
            movementProgress = valueAnimator.animatedValue as Float
            listener.updateMovementProgress(movementProgress, startX, startY, MovementAnimationType.CENTER_LOCATION)
        }
        movementAnimator.start()
    }

    fun startAppearAnimation() {
        animationState = AnimationState.APPEAR
        val appearAnimator = ValueAnimator.ofFloat(0f, 1f)
        appearAnimator.duration = MapView.APPEAR_ANIMATION_DURATION
        appearAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                animationState = AnimationState.NONE
                listener.onEndAppearAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        appearAnimator.addUpdateListener { valueAnimator ->
            appearProgress = valueAnimator.animatedValue as Float
            listener.updateAppearProgress(appearProgress)
            listener.redraw()
        }
        appearAnimator.start()
    }

    fun startWaveAnimation() {
        if (waveAnimator.isRunning) {
            waveAnimator.cancel()
        }
        isWavesEnabled = true

        waveAnimator = ValueAnimator.ofFloat(0f, 1f)
        waveAnimator.duration = MapView.WAVE_ANIMATION_DURATION
        waveAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                waveProgress = 0f
                listener.updateWaveProgress(0f)
                if (isWavesEnabled && animation == waveAnimator) {
                    animation.startDelay = 5000
                    animation.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationStart(animation: Animator) {
            }

        })
        waveAnimator.addUpdateListener { valueAnimator ->
            waveProgress = valueAnimator.animatedValue as Float
            listener.updateWaveProgress(waveProgress)
            listener.redraw()
        }
        waveAnimator.start()
    }

    fun stopWaveAnimation() {
        isWavesEnabled = false
        if (waveAnimator.isRunning) {
            waveAnimator.cancel()
        }
    }

    interface AnimatorListener {
        fun redraw()

        fun onStartMovementAnimation()

        fun updateMovementProgress(progress: Float, startX: Float, startY: Float, animationType: MovementAnimationType)

        fun updateCenterGatewayProgress(progress: Float, startX: Float, startY: Float, location: ServerLocation)

        fun onEndMovementAnimation()

        fun updateAppearProgress(progress: Float)

        fun onEndAppearAnimation()

        fun updateWaveProgress(progress: Float)

        fun onCenterAnimationFinish(locations: ArrayList<ServerLocation>?, dialogState: DialogueDrawer.DialogState)
    }

    enum class AnimationState {
        NONE,
        MOVEMENT,
        APPEAR
    }

    enum class MovementAnimationType {
        CENTER_LOCATION,
        CENTER_GATEWAY
    }
}