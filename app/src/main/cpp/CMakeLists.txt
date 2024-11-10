cmake_minimum_required(VERSION 3.10)

# Nombre de tu proyecto
project(MyApplication)

# Establece la ruta de OpenCV
set(OpenCV_DIR "C:/Users/Admin/Desktop/Tesis/camera dji/update_stream_DJI_library/Android-FPVDemo/FPVDemo/OpenCV/native/jni")

# Encuentra el paquete de OpenCV
find_package(OpenCV 4.10 REQUIRED)

# Añade las bibliotecas nativas
add_library(native-lib SHARED native-lib.cpp)  # Asegúrate de tener el archivo `native-lib.cpp` en la carpeta `cpp`

# Vincula las bibliotecas de OpenCV
target_link_libraries(native-lib ${OpenCV_LIBRARIES})