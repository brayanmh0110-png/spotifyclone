# Explicación Técnica: Integración de la API de Música

Para que este clon de Spotify tenga contenido real y dinámico sin necesidad de subir archivos manualmente a la nube, hemos integrado la **iTunes Search API**.

## 1. ¿Qué es y dónde se usa?
Es una API REST pública proporcionada por Apple. En este proyecto, se utiliza específicamente en el archivo:
`app/src/main/java/com/example/spotifyclone/repository/MusicRepository.kt`

Dentro de la función `seedFullProjectData()`, la aplicación realiza peticiones HTTP a la siguiente URL:
`https://itunes.apple.com/search?term={BUSQUEDA}&limit=1&entity=song`

Para el buscador global, el límite se amplía a 10 resultados para ofrecer variedad.

## 2. El Proceso de Obtención de Datos
1. **Consulta**: La App envía el nombre de una canción y artista. En el buscador, se envía el texto que el usuario escribe en tiempo real.
2. **Respuesta JSON**: La API responde con un objeto que contiene toda la información oficial.
3. **Mapeo**: Nuestro código extrae los siguientes campos:
    - `trackName`: Nombre real de la canción.
    - `artistName`: Nombre del artista.
    - `artworkUrl100`: La carátula del álbum (que modificamos a 600x600 para mayor calidad).
    - `previewUrl`: El enlace directo al archivo de audio MP3.
4. **Almacenamiento/Visualización**: Los datos del sembrado se guardan en **Firestore**, mientras que los resultados del buscador se muestran dinámicamente en la UI.

## 3. Buscador Global en Tiempo Real
El buscador utiliza la misma infraestructura que el sembrado de datos, pero de forma reactiva:
- Cada vez que el usuario escribe, el `MusicViewModel` activa un flujo asíncrono.
- Se obtienen resultados frescos directamente de la API, permitiendo descubrir música nueva en cualquier momento.

## 4. ¿Por qué las canciones duran 30 segundos?
Esta es una **limitación de licencia** de la API de iTunes:
- **Gratuidad**: Apple ofrece estos 30 segundos de forma gratuita y pública para que los desarrolladores puedan crear catálogos de música.
- **Copyright**: Reproducir canciones completas (3-5 minutos) de artistas famosos de forma gratuita es ilegal y requiere licencias comerciales. 
- **Propósito**: Para un proyecto académico o de portafolio, 30 segundos son suficientes para demostrar que el reproductor de audio, la barra de progreso y la interfaz funcionan perfectamente con streaming real.

## 4. Optimización de Rendimiento
Para procesar 50 canciones sin que la App se bloquee, usamos **Corrutinas**:
- Se lanzan 50 peticiones en paralelo usando `async`.
- Esto permite que en menos de 5 segundos, tu base de datos de Firebase esté totalmente llena con música actual de Spotify Perú y el mundo.
