package org.tendiwa.client.gdx

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

class TendiwaGame : ApplicationAdapter() {
    override fun render() {
        Gdx.gl.glClearColor(0.9f, 0.0f, 0.2f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }
}