package org.tendiwa.client.gdx

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction
import com.badlogic.gdx.utils.viewport.FitViewport
import org.tendiwa.backend.existence.StimulusMedium
import org.tendiwa.backend.space.Reality
import org.tendiwa.backend.space.aspects.Position
import org.tendiwa.backend.space.aspects.position
import org.tendiwa.client.gdx.floor.FloorLayer
import org.tendiwa.client.gdx.input.KeysSetup
import org.tendiwa.client.gdx.input.TendiwaInputAdapter
import org.tendiwa.client.gdx.realThings.RealThingActorRegistry
import org.tendiwa.client.gdx.resources.images.NamedTextureCache
import org.tendiwa.client.gdx.walls.WallActorFactory
import org.tendiwa.frontend.generic.PlayerVolition
import org.tendiwa.frontend.generic.RenderingVicinity
import org.tendiwa.frontend.generic.hasWallAt
import org.tendiwa.plane.grid.dimensions.GridDimension

class TendiwaGame(
    private val atlasPath: String,
    private val reality: Reality,
    private val playerVolition: PlayerVolition,
    private val stimulusMedium: StimulusMedium,
    private val plugins: List<TendiwaGdxClientPlugin>
) : ApplicationAdapter() {
    lateinit var textureCache: NamedTextureCache
    lateinit var stage: Stage
    lateinit var vicinity: RenderingVicinity
    lateinit var camera: TendiwaCamera
    lateinit var keysSetup: KeysSetup
    lateinit var realThingActorRegistry: RealThingActorRegistry

    override fun create() {
        initVicinity()
        initFacilities()
        initInput()
        initPlugins()
        initSurroundings()
        initReactions()
    }

    lateinit var wallActorFactory: WallActorFactory

    private fun initSurroundings() {
        vicinity.things.forEach {
            realThingActorRegistry.addRealThing(it)
        }
        stage.apply {
            actionsRequestRendering = false
            addActor(
                FloorLayer(textureCache, vicinity)
            )
            vicinity.tileBounds.forEachTile { x, y ->
                if (
                vicinity.hasWallAt(x, y) && vicinity.fieldOfView.contains(x, y)
                ) {
                    wallActorFactory.createActor(x, y)
                        .let { addActor(it) }
                }
            }
            vicinity.things.forEach {
                val position = it.position.voxel
                if (vicinity.fieldOfView.contains(position.x, position.y)) {
                    realThingActorRegistry.actorOf(it)
                        .let { addActor(it) }
                }
            }
        }
    }

    private fun initReactions() {
        stimulusMedium.subscribeToAll {
            if (it is Position.Change) {
                val actor = realThingActorRegistry
                    .actorOf(it.host)
                actor
                    .addAction(
                        MoveToAction()
                            .apply {
                                this.setPosition(
                                    it.to.x.toFloat(),
                                    it.to.y.toFloat()
                                )
                                this.duration = 0.1f
                            }
                    )
            }
        }
    }

    private fun initInput() {
        val inputAdapter = TendiwaInputAdapter()
        Gdx.input.inputProcessor = inputAdapter
        keysSetup = inputAdapter.keysSetup
    }

    private fun initFacilities() {
        textureCache =
            NamedTextureCache(
                TextureAtlas(
                    Gdx.files.classpath(atlasPath)
                )
            )
        camera = TendiwaCamera()
        wallActorFactory =
            WallActorFactory(
                NamedTextureCache(
                    TextureAtlas(Gdx.files.classpath("walls/walls.atlas"))
                ),
                vicinity
            )
        realThingActorRegistry = RealThingActorRegistry()
        stage = Stage(
            FitViewport(
                Gdx.graphics.width.toFloat() / 32,
                Gdx.graphics.height.toFloat() / 32,
                camera
            ),
            SpriteBatch()
        )
    }

    private fun initVicinity() {
        vicinity = RenderingVicinity(
            reality.space,
            GridDimension(
                Gdx.graphics.width / 32,
                Gdx.graphics.height / 32
            )
        )
    }

    private fun initPlugins() {
        plugins.forEach {
            it.init(
                camera,
                vicinity,
                playerVolition,
                keysSetup,
                reality
            )
        }
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }
}

