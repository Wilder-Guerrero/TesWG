# TesWG
Project code, links of interest, description, documentation ...

Proyecto de Control Autónomo de Dron DJI Air 2S con Visión por Computadora
Este proyecto implementa una plataforma de control autónomo para el dron DJI Air 2S mediante visión por computadora, utilizando herramientas de OpenCV en un entorno de desarrollo Android. La finalidad es desarrollar una solución que permita al dron realizar diversas tareas de reconocimiento visual en tiempo real, incluyendo seguimiento y reconocimiento de objetos y rostros, con el fin de mejorar su autonomía y capacidad de reacción.

Descripción del Proyecto
El sistema aprovecha el SDK de DJI para Android junto con OpenCV para implementar funciones avanzadas de visión, tales como:

Reconocimiento Facial: Detección y seguimiento de rostros en tiempo real para permitir aplicaciones como el seguimiento de personas o la identificación en un área determinada.
Detección de Marcadores (Aruco): Intentamos implementar la detección de marcadores Aruco para obtener datos de orientación y distancia. Aunque experimentamos problemas en la implementación directa, documentamos los avances y pruebas realizadas.
Filtros y Detección de Bordes: Se aplicaron filtros de OpenCV para mejorar la claridad de la imagen, realizar detección de bordes y aplicar técnicas de suavizado y afilado, lo que permite que el dron interprete mejor las características visuales de su entorno.
Seguimiento de Objetos: Usando reconocimiento de patrones y cálculo de orientación, el dron puede seguir a objetos en movimiento manteniendo una distancia y ángulo constantes, basándose en las coordenadas calculadas mediante visión por computadora.

Tecnologías Utilizadas
DJI Mobile SDK: Proporciona acceso a las funciones de control del dron DJI Air 2S.
OpenCV: Librería de visión por computadora para procesamiento de imágenes en tiempo real.
Android Studio: Entorno de desarrollo para aplicaciones Android, utilizado para integrar y compilar los módulos del SDK de DJI y OpenCV.

Instrucciones de Uso
Requisitos Previos: Asegúrate de tener el SDK de DJI y OpenCV correctamente instalados en el proyecto.
Configuración: Configura los parámetros de cámara y de procesamiento de video para adaptarlos al entorno en el que se utilizará el dron.
Ejecución del Proyecto: Conecta el dron DJI Air 2S, inicia la aplicación en tu dispositivo Android y selecciona las funciones de visión que deseas activar desde la interfaz.
Actualización de Código: El repositorio está configurado para que cualquier cambio en el código pueda subirse fácilmente a GitHub mediante Git.

Avances y Documentación
Este repositorio contiene documentación de cada fase del proyecto, desde la implementación inicial hasta la optimización de procesamiento en tiempo real. También se incluyen los retos enfrentados y los cambios de estrategias en la implementación de ciertos algoritmos de OpenCV para mejorar el rendimiento en dispositivos de recursos limitados.
