# DuocRacing: Proyecto Educativo de Programacion Orientada a Objetos

Este proyecto consiste en una implementación práctica de un videojuego de carreras desarrollado en Java. Su objetivo principal es demostrar y consolidar conceptos avanzados de Programación Orientada a Objetos (POO) mediante una aplicación visual e interactiva, diferenciándose de los ejercicios teóricos tradicionales.

## Conceptos de POO Aplicados

El núcleo del proyecto se basa en una arquitectura de clases diseñada para la extensibilidad y el mantenimiento.

### 1. Abstraccion y Clases Abstractas
**Clase:** Juego (Paquete: model)

Se utiliza una clase padre abstracta llamada Juego para evitar la duplicación de código.
* **Proposito:** Define la estructura base (coordenadas x e y, dimensiones, textura) y obliga a las subclases a implementar comportamientos específicos mediante el método abstracto update().
* **Justificacion:** No se instancian objetos genéricos de tipo "Juego", solo entidades concretas como autos o barreras.

### 2. Herencia
**Clases:** AutoJugador, AutoEnemigo, Barrera

Estas clases extienden de la clase abstracta Juego.
* Heredan automáticamente atributos y métodos de la clase padre.
* Facilita la reutilización de código y la especialización de comportamientos.

### 3. Polimorfismo
**Implementacion:** List<Juego> en la clase Main

El sistema gestiona los objetos de manera genérica, sin necesidad de conocer su tipo concreto en tiempo de compilación.
* Se utiliza una lista polimórfica que almacena instancias de AutoEnemigo y Barrera simultáneamente.
* En el ciclo de juego, se invoca el método update() de cada objeto. Java determina en tiempo de ejecución cuál es la implementación correcta a ejecutar (enlace dinámico).

### 4. Interfaces
**Interfaz:** Chocable (Paquete: interfaces)

Define un contrato de comportamiento independiente de la jerarquía de clases.
* **Contrato:** Cualquier clase que implemente Chocable debe definir el método chocoEnLaCarrera().
* **Ventaja:** Permite desacoplar la lógica de colisión de la herencia básica. Si en el futuro se agregan elementos decorativos (como nubes), estos heredarán de Juego pero no implementarán Chocable, evitando comportamientos erróneos.

### 5. Encapsulamiento
Se utilizan modificadores de acceso (protected, private) para proteger el estado interno de los objetos, exponiendo solo los métodos necesarios (getters y setters) para la manipulación controlada desde la clase principal.

## Tecnologia Utilizada

* **Lenguaje:** Java (JDK 11 o superior).
* **Framework:** LibGDX (Ciclo de vida de la aplicación y renderizado gráfico).
* **Gestor de Construccion:** Gradle.

## Estructura del Proyecto

com.duoc.race
├── interfaces
│   └── Chocable.java       // Contrato para objetos colisionables
├── model
│   ├── Juego.java          // Clase Padre Abstracta
│   ├── AutoJugador.java    // Vehículo controlado por el usuario
│   ├── AutoEnemigo.java    // Obstáculo móvil
│   └── Barrera.java        // Obstáculo estático
└── Main.java               // Lógica principal y ciclo de renderizado

## Instrucciones de Ejecucion

Para ejecutar el proyecto, se recomienda utilizar el Wrapper de Gradle incluido en el repositorio. Esto asegura que se utilice la versión correcta de Gradle sin necesidad de instalación manual.

### Prerrequisitos
* Tener instalado un JDK (Java Development Kit) versión 11 o superior.
* Tener configurada la variable de entorno JAVA_HOME.

### Ejecucion desde la Terminal

Abra una terminal en la carpeta raíz del proyecto y ejecute el siguiente comando segun su sistema operativo:

**En Windows:**
```bash
./gradlew lwjgl3:run
