package com.duoc.race;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.duoc.race.interfaces.Chocable;
import com.duoc.race.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main extends ApplicationAdapter {

    // -------------------------------------------------------------------------
    // 1. CONSTANTES DE CONFIGURACIÓN DEL JUEGO
    // -------------------------------------------------------------------------
    private static final float PLAYER_SPEED      = 400f;
    private static final float SCROLL_SPEED      = 500f;
    private static final float SPAWN_INTERVAL    = 0.8f;
    private static final float SCORE_INTERVAL    = 0.1f;
    private static final int   INITIAL_LIFE      = 100;
    private static final int   DAMAGE_ENEMY      = 30;
    private static final int   DAMAGE_BARRIER    = 10;
    private static final float DESPAWN_Y         = -150f;
    private static final int   LEFT_BOUND        = 80;
    private static final int   RIGHT_MARGIN      = 80;
    // Nuevas constantes para las nubes
    private static final float CLOUD_SPAWN_INTERVAL = 1.8f;

    // -------------------------------------------------------------------------
    // 2. RENDERING (HERRAMIENTAS GRÁFICAS)
    // -------------------------------------------------------------------------
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture texJugador;
    private Texture texEnemigo;
    private Texture texBarrera;
    private Texture texPista;
    private Texture texTribuna;
    private Texture texNube;

    // -------------------------------------------------------------------------
    // 3. MODELO DEL JUEGO (OBJETOS Y ESTADO)
    // -------------------------------------------------------------------------
    private AutoJugador jugador;
    private List<Juego> obstaculos;
    private List<Juego> nubes;    // <-- LISTA DE NUBES DECORATIVAS

    private float scrollY        = 0f;
    private float tiempoSpawn    = 0f;
    private float tiempoPuntaje  = 0f;
    private float tiempoSpawnNube = 0f;  // <-- TIMER PARA NUBES
    private int   puntaje        = 0;
    private int   vida           = INITIAL_LIFE;
    private boolean gameOver     = false;

    // -------------------------------------------------------------------------
    // 4. CICLO DE VIDA LIBGDX
    // -------------------------------------------------------------------------

    /**
     * Inicializa el juego al momento de arrancar la aplicación.
     * <p>
     * Este método forma parte del ciclo de vida estándar de LibGDX y se ejecuta
     * **una sola vez** al inicio del programa. Su responsabilidad es preparar
     * todos los recursos necesarios para que el juego pueda comenzar: configurar
     * herramientas de renderizado, cargar texturas y construir los objetos base
     * del mundo.
     * <p>
     * Flujo interno:
     * <ul>
     *     <li><b>initRendering()</b>: Inicializa el motor gráfico del juego
     *     (SpriteBatch y BitmapFont), que se utilizarán para dibujar en pantalla.</li>
     *
     *     <li><b>loadTexturesSafely()</b>: Carga las imágenes desde el disco. Si
     *     alguna textura falta o se produce un error crítico, el método devuelve
     *     <code>false</code>, y el juego no continúa para evitar fallas.</li>
     *
     *     <li><b>initWorld()</b>: Crea los objetos principales del juego, como el
     *     auto del jugador y la lista de obstáculos, además de establecer el estado
     *     inicial (vida, puntaje, timers, etc.).</li>
     * </ul>
     * <p>
     * En resumen, este método prepara todo lo necesario antes de que el ciclo
     * de <code>render()</code> comience a ejecutarse 60 veces por segundo.
     */
    @Override
    public void create() {
        initRendering();
        if (!loadTexturesSafely()) {
            // Si algo falló cargando texturas, no seguimos
            return;
        }
        initWorld();
    }

    /**
     * Ciclo principal del juego. Este método es llamado automáticamente por LibGDX
     * aproximadamente 60 veces por segundo, y representa el "bucle de juego"
     * (game loop) clásico.
     * <p>
     * Su responsabilidad es coordinar todas las acciones que ocurren en cada
     * frame: calcular el tiempo transcurrido, actualizar la lógica del juego según
     * el estado actual, y finalmente dibujar los elementos en pantalla.
     *
     * <p>Flujo interno:</p>
     * <ul>
     *     <li><b>delta = Gdx.graphics.getDeltaTime()</b>: Obtiene el tiempo real
     *     transcurrido entre el frame anterior y el actual, lo que permite que
     *     los movimientos y actualizaciones sean suaves e independientes de la velocidad
     *     del CPU.</li>
     *
     *     <li><b>updateRunningState(delta)</b>: Si el juego está en curso
     *     (no hay Game Over), se actualiza toda la lógica principal: movimiento
     *     del jugador, generación de obstáculos, scroll de la pista, detección de
     *     choques, puntaje, etc.</li>
     *
     *     <li><b>updateGameOverState()</b>: Si el jugador ha perdido,
     *     este método gestiona las opciones disponibles en la pantalla de Game Over
     *     (reiniciar o salir del juego).</li>
     *
     *     <li><b>drawFrame()</b>: Finalmente, una vez actualizada la lógica del
     *     frame, se dibuja todo en pantalla (fondo, autos, enemigos, HUD,
     *     mensajes, etc.).</li>
     * </ul>
     *
     * <p>
     * En resumen, este método es el corazón del juego: calcula, actualiza y dibuja,
     * manteniendo viva la simulación carrera a carrera.
     * </p>
     */
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (!gameOver) {
            updateRunningState(delta);
        } else {
            updateGameOverState();
        }

        drawFrame();
    }

    /**
     * Libera los recursos utilizados por la aplicación antes de cerrar.
     * <p>
     * Este método forma parte del ciclo de vida estándar de LibGDX y es llamado
     * automáticamente cuando la aplicación se cierra. Su objetivo es evitar fugas
     * de memoria liberando correctamente todos los recursos gráficos cargados,
     * especialmente texturas y objetos que residen en la memoria de la tarjeta gráfica (VRAM).
     * </p>
     *
     * <p>Flujo interno:</p>
     * <ul>
     *     <li><b>disposeRenderingResources()</b>: Encapsula la liberación de
     *     SpriteBatch, BitmapFont y todas las texturas utilizadas en el juego.
     *     Esto mantiene la responsabilidad única del método y facilita su
     *     mantenimiento.</li>
     * </ul>
     *
     * <p>
     * En resumen, este método garantiza que el juego se cierre de forma limpia y
     * responsable, liberando todos los recursos gráficos que fueron cargados
     * durante la ejecución.
     * </p>
     */
    @Override
    public void dispose() {
        disposeRenderingResources();
    }

    // -------------------------------------------------------------------------
    // 5. INICIALIZACIÓN
    // -------------------------------------------------------------------------

    /**
     * Inicializa todos los recursos relacionados con el sistema de renderizado.
     * <p>
     * Este método se ejecuta durante la etapa de creación del juego y prepara
     * las herramientas esenciales para dibujar en pantalla. Su responsabilidad
     * es configurar el entorno gráfico que utilizará el motor de LibGDX en cada
     * frame.
     * </p>
     *
     * <p>Elementos inicializados:</p>
     * <ul>
     *     <li><b>SpriteBatch</b>: Herramienta fundamental para dibujar eficientemente
     *         imágenes y texto en la pantalla. Funciona como un “lienzo optimizado”.</li>
     *
     *     <li><b>BitmapFont</b>: Fuente utilizada para renderizar texto, como
     *         el puntaje o la vida del jugador. Aquí se escala para mejorar su
     *         visibilidad dentro de la interfaz del juego.</li>
     * </ul>
     *
     * <p>
     * Este método concentra toda la lógica de configuración gráfica inicial,
     * manteniendo el principio de responsabilidad única y facilitando el
     * mantenimiento futuro.
     * </p>
     */
    private void initRendering() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);
    }

    /**
     * Carga todas las texturas necesarias para el juego de forma segura.
     * <p>
     * Este método se encarga de transferir las imágenes desde el disco a la memoria
     * de video (VRAM). Debido a que la carga de archivos externos puede fallar
     * —por rutas incorrectas, archivos ausentes o problemas del sistema— el método
     * implementa un enfoque robusto basado en <code>try/catch</code>.
     * </p>
     *
     * <p>Flujo y responsabilidades:</p>
     * <ul>
     *     <li><b>Carga de texturas principales</b>:
     *         Se cargan las imágenes asociadas al jugador, enemigos, barreras y
     *         tribunas. Cada una es esencial para la representación visual del juego.</li>
     *
     *     <li><b>Carga segura de la pista</b>:
     *         Antes de cargar la textura de la pista (<code>Road19.jpg</code>),
     *         el método verifica si el archivo realmente existe.
     *         <ul>
     *             <li>Si existe → se carga la textura normal.</li>
     *             <li>Si NO existe → se crea una textura mínima (1×1 píxel),
     *                 evitando que el juego falle por un recurso faltante.</li>
     *         </ul>
     *         Este comportamiento actúa como un “fallback” visual seguro.</li>
     *
     *     <li><b>Configuración de repetición (wrapping)</b>:
     *         Se configuran las texturas grandes (pista y tribuna) para repetirse
     *         verticalmente, permitiendo el efecto de desplazamiento infinito
     *         durante la carrera.</li>
     *
     *     <li><b>Control de errores</b>:
     *         Cualquier excepción en la carga es capturada. En ese caso el método
     *         informa el error por consola y retorna <code>false</code>, señalando
     *         a la rutina de inicialización que no es seguro continuar.</li>
     * </ul>
     *
     * <p>
     * En resumen, este método implementa una estrategia de carga defensiva que
     * garantiza que el juego pueda iniciar incluso si falta alguna textura, evitando
     * bloqueos o cierres inesperados durante la etapa de creación.
     * </p>
     *
     * @return <code>true</code> si todas las texturas fueron cargadas correctamente,
     *         <code>false</code> si ocurrió un error crítico.
     */
    private boolean loadTexturesSafely() {
        try {
            texJugador  = new Texture("car_blue_1.png");
            texEnemigo  = new Texture("car_black_small_5.png");
            texBarrera  = new Texture("barrier_red_race.png");
            texTribuna  = new Texture("tribune_full.png");
            texNube     = new Texture("cloud.PNG"); // <-- Asegúrate de tener este asset

            if (Gdx.files.internal("Road19.jpg").exists()) {
                texPista = new Texture("Road19.jpg");
            } else {
                texPista = new Texture(
                    new com.badlogic.gdx.graphics.Pixmap(
                        1, 1,
                        com.badlogic.gdx.graphics.Pixmap.Format.RGB888
                    )
                );
            }

            texPista.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            texTribuna.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

            return true;

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO CARGANDO IMÁGENES: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inicializa los elementos principales del mundo del juego.
     * <p>
     * Este método se ejecuta después de cargar correctamente las texturas y tiene
     * como responsabilidad construir los objetos base que dan vida al escenario:
     * el jugador, la lista de obstáculos y el estado inicial del juego.
     * </p>
     *
     * <p>Flujo interno:</p>
     * <ul>
     *     <li><b>Posicionamiento del jugador</b>:
     *         Se calcula la posición horizontal central en pantalla y se instancia
     *         el objeto <code>AutoJugador</code> utilizando su textura asociada.</li>
     *
     *     <li><b>Creación de la lista polimórfica de obstáculos</b>:
     *         Se inicializa un <code>ArrayList&lt;Juego&gt;</code>, que almacenará
     *         tanto autos enemigos como barreras, aprovechando el polimorfismo de
     *         la jerarquía de clases.</li>
     *
     *     <li><b>resetGameState()</b>:
     *         Se encarga de establecer los valores iniciales del juego
     *         (vida, puntaje, timers, estado Game Over, etc.) y ubicar nuevamente
     *         al jugador en su posición inicial.
     *         Esto encapsula el comportamiento de “nuevo comienzo” y evita la
     *         duplicación de lógica.</li>
     * </ul>
     *
     * <p>
     * En resumen, este método construye el mundo lógico del juego y lo deja en un
     * estado consistente para iniciar la partida.
     * </p>
     */
    private void initWorld() {
        int centerX = Gdx.graphics.getWidth() / 2 - 30;
        jugador = new AutoJugador(centerX, 50, texJugador);
        obstaculos = new ArrayList<>();
        nubes = new ArrayList<>();
        resetGameState();
    }

    /**
     * Restablece todas las variables críticas del juego para comenzar una partida nueva.
     * <p>
     * Este método se utiliza al iniciar el mundo por primera vez y también cuando el
     * jugador pierde y decide reiniciar. Su responsabilidad es dejar todos los valores
     * del estado del juego en condiciones iniciales, garantizando un punto de partida
     * consistente.
     * </p>
     *
     * <p>Acciones realizadas:</p>
     * <ul>
     *     <li>Restablece vida, puntaje y temporizadores (spawn y score).</li>
     *     <li>Limpia la lista de obstáculos, eliminando cualquier enemigo previo.</li>
     *     <li>Reubica al jugador en el centro de la pantalla.</li>
     *     <li>Desactiva el estado de Game Over.</li>
     * </ul>
     */
    private void resetGameState() {
        vida = INITIAL_LIFE;
        puntaje = 0;
        tiempoPuntaje = 0;
        tiempoSpawn = 0;
        tiempoSpawnNube = 0f;   // <-- REINICIAMOS TIMER DE NUBES
        gameOver = false;
        obstaculos.clear();
        nubes.clear();          // <-- LIMPIAMOS NUBES EXISTENTES

        int centerX = Gdx.graphics.getWidth() / 2 - 30;
        jugador.setX(centerX);
    }

// -----------------------------------------------------------------------------
// 6. ACTUALIZACIÓN DEL ESTADO (LÓGICA DEL JUEGO)
// -----------------------------------------------------------------------------

    /**
     * Actualiza toda la lógica del juego mientras la partida está activa.
     * <p>
     * Este método centraliza las operaciones que ocurren en cada frame del juego
     * cuando el jugador aún no ha perdido. Sus responsabilidades están delegadas
     * en métodos más específicos, reforzando la responsabilidad única.
     * </p>
     *
     * @param delta tiempo transcurrido entre el frame anterior y el actual.
     */
    private void updateRunningState(float delta) {
        handlePlayerInput(delta);
        updateScrollAndTimers(delta);
        spawnCloudsIfNeeded();        // <-- NUEVO: SPAWN DE NUBES
        spawnObstaclesIfNeeded();
        updateScoreIfNeeded(delta);
        updateObstaclesAndCollisions(delta);
        updateClouds(delta);          // <-- NUEVO: ACTUALIZAR NUBES
    }

    /**
     * Gestiona el comportamiento del juego cuando el estado es "Game Over".
     * <p>
     * Permite que el jugador reinicie la partida o cierre la aplicación.
     * Este método evita mezclar la lógica de juego activo con la lógica de fin
     * de partida.
     * </p>
     */
    private void updateGameOverState() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetGameState();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    /**
     * Procesa la entrada del jugador (teclas izquierda y derecha) y mueve el auto.
     * <p>
     * También garantiza que el jugador no salga de los límites de la pista mediante
     * un <code>clamp</code> de la posición X.
     * </p>
     *
     * @param delta tiempo transcurrido entre frames.
     */
    private void handlePlayerInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            jugador.setX(jugador.getX() - PLAYER_SPEED * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            jugador.setX(jugador.getX() + PLAYER_SPEED * delta);
        }

        float minX = LEFT_BOUND;
        float maxX = Gdx.graphics.getWidth() - RIGHT_MARGIN - jugador.getWidth();
        float clampedX = MathUtils.clamp(jugador.getX(), minX, maxX);
        jugador.setX(clampedX);
    }

    /**
     * Actualiza el scroll vertical de la pista y los temporizadores internos.
     * <p>
     * Este método controla el movimiento del fondo y la medición del tiempo
     * necesario para spawn de enemigos y acumulación de puntaje.
     * </p>
     */
    private void updateScrollAndTimers(float delta) {
        scrollY -= SCROLL_SPEED * delta;
        tiempoSpawn += delta;
        tiempoPuntaje += delta;
        tiempoSpawnNube += delta;     // <-- AVANZA TEMPORIZADOR DE NUBES
    }

    /**
     * Genera enemigos u obstáculos cuando el temporizador de spawn supera el umbral configurado.
     * <p>
     * El método utiliza probabilidad para decidir si el nuevo objeto será un
     * <code>AutoEnemigo</code> o una <code>Barrera</code>. Todos los objetos se
     * agregan a la lista polimórfica <code>List&lt;Juego&gt;</code>.
     * </p>
     */
    private void spawnObstaclesIfNeeded() {
        if (tiempoSpawn <= SPAWN_INTERVAL) {
            return;
        }

        float randomX = MathUtils.random(90, Gdx.graphics.getWidth() - 130);

        if (MathUtils.randomBoolean(0.6f)) {
            obstaculos.add(new AutoEnemigo(randomX, Gdx.graphics.getHeight(), texEnemigo));
        } else {
            obstaculos.add(new Barrera(randomX, Gdx.graphics.getHeight(), texBarrera));
        }

        tiempoSpawn = 0f;
    }

    /**
     * Incrementa el puntaje del jugador según el temporizador interno.
     * <p>
     * La puntuación aumenta cada cierto intervalo fijo, independiente del frame rate,
     * permitiendo una progresión consistente.
     * </p>
     */
    private void updateScoreIfNeeded(float delta) {
        if (tiempoPuntaje > SCORE_INTERVAL) {
            puntaje += 1;
            tiempoPuntaje = 0f;
        }
    }

    /**
     * Actualiza cada obstáculo, verifica colisiones con el jugador y elimina
     * los objetos que salen de la pantalla.
     * <p>
     * El método recorre la lista polimórfica de objetos del juego, aplicando
     * <code>update()</code> a cada uno. También realiza detección de colisiones
     * y coordina la eliminación segura mediante un iterador.
     * </p>
     *
     * @param delta tiempo transcurrido entre frames.
     */
    private void updateObstaclesAndCollisions(float delta) {
        Iterator<Juego> iter = obstaculos.iterator();

        while (iter.hasNext()) {
            Juego obj = iter.next();

            obj.update(delta);

            if (obj.getBounds().overlaps(jugador.getBounds())) {
                handleCollision(obj);
                iter.remove();
                if (gameOver) {
                    continue;
                }
            }

            if (obj.getY() < DESPAWN_Y) {
                iter.remove();
            }
        }
    }

    /**
     * Procesa una colisión entre el jugador y un obstáculo específico.
     * <p>
     * Ejecuta la reacción definida por la interfaz <code>Chocable</code> si aplica,
     * aplica daño según el tipo de objeto, y actualiza el estado de vida del jugador.
     * Si la vida llega a cero, marca el estado de Game Over.
     * </p>
     *
     * @param obj el objeto con el que el jugador colisionó.
     */
    private void handleCollision(Juego obj) {
        if (obj instanceof Chocable) {
            ((Chocable) obj).chocoEnLaCarrera();
        }

        if (obj instanceof AutoEnemigo) {
            vida -= DAMAGE_ENEMY;
        } else if (obj instanceof Barrera) {
            vida -= DAMAGE_BARRIER;
        }

        if (vida <= 0) {
            gameOver = true;
        }
    }

// -----------------------------------------------------------------------------
// 7. DIBUJADO (RENDER)
// -----------------------------------------------------------------------------

    /**
     * Dibuja un frame completo del juego.
     * <p>
     * Este método se ejecuta en cada ciclo y se encarga del proceso de renderizado.
     * Internamente delega en métodos especializados la responsabilidad de dibujar
     * fondo, jugador, enemigos, HUD y mensajes de Game Over.
     * </p>
     */
    private void drawFrame() {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();

        renderBackground();
        renderClouds();      // <-- NUBES SOBRE EL FONDO
        renderPlayer();
        renderObstacles();
        renderHUD();
        renderGameOverMessageIfNeeded();

        batch.end();
    }

    /**
     * Renderiza el fondo del juego: pista central y tribunas laterales.
     * <p>
     * Utiliza texturas configuradas en modo repetición para dar la sensación visual
     * de desplazamiento infinito hacia abajo.
     * </p>
     */
    private void renderBackground() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Pista central
        batch.draw(
            texPista,
            LEFT_BOUND, 0,
            width - LEFT_BOUND * 2f,
            height,
            0, (int) scrollY,
            texPista.getWidth(), texPista.getHeight(),
            false, false
        );

        // Tribunas laterales
        batch.draw(
            texTribuna,
            0, 0,
            LEFT_BOUND,
            height,
            0, (int) scrollY,
            texTribuna.getWidth(), texTribuna.getHeight(),
            false, false
        );

        batch.draw(
            texTribuna,
            width - LEFT_BOUND,
            0,
            LEFT_BOUND,
            height,
            0, (int) scrollY,
            texTribuna.getWidth(), texTribuna.getHeight(),
            false, false
        );
    }

    /**
     * Dibuja el auto del jugador en pantalla.
     */
    private void renderPlayer() {
        batch.draw(jugador.texture, jugador.getX(), jugador.getY(), jugador.getWidth(), jugador.getHeight());
    }

    /**
     * Renderiza todos los obstáculos presentes en el mundo (autos enemigos y barreras).
     * <p>
     * Se recorre la lista polimórfica <code>List&lt;Juego&gt;</code> para dibujar
     * cada objeto con su textura correspondiente.
     * </p>
     */
    private void renderObstacles() {
        for (Juego obj : obstaculos) {
            batch.draw(obj.texture, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
        }
    }

    /**
     * Renderiza elementos de interfaz gráfica: puntaje y vida.
     * <p>
     * La vida cambia de color según su nivel para dar feedback visual inmediato
     * al jugador.
     * </p>
     */
    private void renderHUD() {
        int height = Gdx.graphics.getHeight();

        // Score
        font.setColor(Color.WHITE);
        font.draw(batch, "Score: " + puntaje, 20, height - 20);

        // Vida
        if (vida > 50) {
            font.setColor(Color.GREEN);
        } else {
            font.setColor(Color.RED);
        }
        font.draw(batch, "Vida: " + vida + "%", 20, height - 50);
    }

    /**
     * Muestra los mensajes de Game Over cuando la partida termina.
     * <p>
     * Indica al jugador que puede reiniciar o salir del juego.
     * El tamaño y color del texto se ajustan dinámicamente para mayor impacto visual.
     * </p>
     */
    private void renderGameOverMessageIfNeeded() {
        if (!gameOver) return;

        int width  = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        font.setColor(Color.RED);
        font.getData().setScale(3);
        font.draw(batch, "GAME OVER", width / 2f - 120, height / 2f + 50);

        font.setColor(Color.YELLOW);
        font.getData().setScale(1.5f);
        font.draw(batch, "¿CONTINUAR?", width / 2f - 80, height / 2f - 20);
        font.getData().setScale(2);
    }

// -----------------------------------------------------------------------------
// 8. LIBERACIÓN DE RECURSOS
// -----------------------------------------------------------------------------

    /**
     * Libera todos los recursos gráficos utilizados por el juego.
     * <p>
     * Este método se asegura de que no queden recursos ocupando memoria en la GPU
     * después de cerrar la aplicación, evitando fugas de memoria.
     * </p>
     */
    private void disposeRenderingResources() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (texJugador != null) texJugador.dispose();
        if (texEnemigo != null) texEnemigo.dispose();
        if (texBarrera != null) texBarrera.dispose();
        if (texPista != null) texPista.dispose();
        if (texTribuna != null) texTribuna.dispose();
        if (texNube != null) texNube.dispose();   // <-- IMPORTANTE
    }

    /**
     * Genera nubes decorativas cuando el temporizador supera el intervalo.
     * <p>
     * Las nubes se crean en posiciones aleatorias en la parte superior de la pantalla
     * y se desplazan hacia abajo. No afectan al jugador ni causan daño.
     * </p>
     */
    private void spawnCloudsIfNeeded() {
        if (tiempoSpawnNube <= CLOUD_SPAWN_INTERVAL) {
            return;
        }

        float randomX = MathUtils.random(0, Gdx.graphics.getWidth() - 150);
        float startY = Gdx.graphics.getHeight() + 50;

        nubes.add(new Nube(randomX, startY, texNube));

        tiempoSpawnNube = 0f;
    }

    /**
     * Actualiza la posición de todas las nubes y elimina las que salen de la pantalla.
     *
     * @param delta tiempo transcurrido entre frames.
     */
    private void updateClouds(float delta) {
        Iterator<Juego> iter = nubes.iterator();

        while (iter.hasNext()) {
            Juego nube = iter.next();
            nube.update(delta);

            if (nube.getY() < DESPAWN_Y) {
                iter.remove();
            }
        }
    }

    /**
     * Renderiza todas las nubes en pantalla.
     * <p>
     * Se dibujan por encima del fondo pero por detrás del HUD.
     * </p>
     */
    private void renderClouds() {
        for (Juego nube : nubes) {
            batch.draw(nube.texture, nube.getX(), nube.getY(), nube.getWidth(), nube.getHeight());
        }
    }


}
