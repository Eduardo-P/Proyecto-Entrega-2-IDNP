# Protocolo de las pruebas de usabilidad (Tarea 4-B)

Guía con la que se hicieron las pruebas. Los resultados están en [`HOJA_RESULTADOS_USABILIDAD.md`](HOJA_RESULTADOS_USABILIDAD.md).

## 1. Preparación
- **App:** build debug en modo demo (`alertauni.demo=true`), con la cuenta **Docente Demo A** y los 40 anuncios de prueba.
- **Participantes:** 2 personas ajenas al equipo.
- **Material:** teléfono con la app, cronómetro y la hoja de resultados.

## 2. Consentimiento (leído en voz alta)
«Gracias por ayudarnos. Vamos a probar la aplicación, no a ti: si algo cuesta, es un problema de la app. Te pediremos tres tareas. Piensa en voz alta mientras las haces. No registramos tu nombre; solo anotamos lo que ocurre. Puedes parar cuando quieras. ¿Aceptas participar?»

## 3. Guion
- Contexto: «Imagina que eres docente y usas esta app para avisar cosas a tus estudiantes».
- No se explica la interfaz. Se lee cada tarea y se deja que la persona la resuelva sola.
- Si pide ayuda, primero se le pregunta «¿Qué harías si yo no estuviera?». Solo se ayuda tras 2 minutos sin avance, y se anota.

## 4. Tareas

| Tarea | Indicación al participante | Completada cuando… |
|---|---|---|
| T1. Buscar un anuncio | Encontrar el anuncio «Primera práctica calificada», de prioridad Alta. | Lo encuentra en el muro. |
| T2. Anuncio privado | Enviar un anuncio **privado** a **Luis Huamán Ccama** de **INF-01**, con prioridad Media, título «titulo» y contenido «contenido». | Lo envía como privado al estudiante correcto. |
| T3. Borrador | Empezar un anuncio **público** titulado «Salida de campo», guardarlo como borrador y volver al muro; después, buscar el borrador, completarlo y publicarlo. | Recupera el borrador y lo publica. |

En T2 y T3 se dan textos simples para medir el uso de la interfaz y no la redacción. En T2, INF-01 tiene otro estudiante llamado Luis (Luis Zúñiga Paz); elegirlo cuenta como error crítico.

## 5. Qué se mide
- **Por tarea:** si la completa, el tiempo, los errores y si pide ayuda.
- **Después de cada tarea, la pregunta SEQ:** «En general, ¿qué tan fácil o difícil fue esta tarea?», de 1 = muy difícil a 7 = muy fácil.
- **Al final:**
  - el cuestionario **SUS** (10 afirmaciones de 1 a 5, con puntuación de 0 a 100);
  - los comentarios libres del participante.

**Errores que se vigilan:**
- buscar en otra pantalla o no ver el buscador;
- publicar como público un anuncio que debía ser privado;
- elegir otro estudiante u otro curso;
- no encontrar «Guardar borrador» ni la pantalla de Borradores;
- salir sin guardar.

## 6. Criterios de éxito (definidos en el examen)

| Criterio | Meta |
|---|---|
| Tareas completadas sin ayuda | ≥ 90 % |
| Envíos al destinatario equivocado (T2) | 0 |
| SEQ promedio por tarea | ≥ 5,5 |
| SUS promedio | ≥ 68 |
