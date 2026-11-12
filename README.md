# ✅ todo-hexagonal

API REST de gestión de tareas (*To-Do*) construida con **Spring Boot** con un objetivo **educativo**: aprender y practicar la **Arquitectura Hexagonal** (también conocida como *Ports & Adapters*).

El dominio es deliberadamente sencillo (crear, listar, consultar, completar y reabrir tareas) para que la atención esté puesta en **cómo se organiza el código**, no en la complejidad del negocio.

---

## 📚 Tabla de contenidos

- [Tecnologías](#-tecnologías)
- [¿Qué es la Arquitectura Hexagonal?](#-qué-es-la-arquitectura-hexagonal)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Las capas en detalle](#-las-capas-en-detalle)
- [Flujo de una petición](#-flujo-de-una-petición)
- [Intercambiar adaptadores](#-intercambiar-adaptadores-la-gran-ventaja)
- [Endpoints](#-endpoints)
- [Manejo de errores](#-manejo-de-errores)
- [Cómo ejecutar](#-cómo-ejecutar)
- [Conceptos aprendidos](#-conceptos-aprendidos)
- [Posibles mejoras](#-posibles-mejoras)

---

## 🛠 Tecnologías

| Tecnología | Uso |
|---|---|
| Java 25 | Lenguaje |
| Spring Boot 4.1 | Framework (Web MVC, Validation, Data JPA) |
| H2 | Base de datos en memoria |
| Lombok | Reducción de código repetitivo (`@Getter`, `@Builder`, …) |
| Maven | Gestión de dependencias y build |

---

## 🧩 ¿Qué es la Arquitectura Hexagonal?

La idea central es **proteger el núcleo de la aplicación (el dominio y los casos de uso) de los detalles técnicos** como el framework web, la base de datos o cualquier servicio externo.

```
            ┌──────────────────── INFRAESTRUCTURA ────────────────────┐
            │                                                         │
 HTTP ────► │  TaskController                JpaTaskRepositoryAdapter │ ──► H2
            │  (adaptador IN)                (adaptador OUT)          │
            │        │                              ▲                 │
            │        ▼                              │                 │
            │  ┌──────────────────── APLICACIÓN ─────────────────┐    │
            │  │  port/in  ──►  TaskService  ──►  port/out       │    │
            │  │                     │                           │    │
            │  │          ┌──────── DOMINIO ────────┐            │    │
            │  │          │  Task, TaskStatus       │            │    │
            │  │          │  TaskNotFoundException  │            │    │
            │  │          └─────────────────────────┘            │    │
            │  └─────────────────────────────────────────────────┘    │
            └─────────────────────────────────────────────────────────┘
```

**Regla de oro — las dependencias siempre apuntan hacia adentro:**

```
infrastructure  ──►  application  ──►  domain
```

- El **dominio** no conoce a nadie (ni Spring, ni JPA, ni HTTP).
- La **aplicación** solo conoce al dominio y define *interfaces* (puertos).
- La **infraestructura** implementa esos puertos y conoce a todos.

| Concepto | Qué es | En este proyecto |
|---|---|---|
| **Puerto de entrada** (*driving port*) | Interfaz que expone lo que la aplicación **sabe hacer** | `CreateTaskUseCase`, `CompleteTaskUseCase`, … |
| **Puerto de salida** (*driven port*) | Interfaz que expresa lo que la aplicación **necesita** del exterior | `TaskRepositoryPort` |
| **Adaptador de entrada** | Traduce el mundo exterior → llamada a un caso de uso | `TaskController` (REST) |
| **Adaptador de salida** | Implementa un puerto de salida con una tecnología concreta | `JpaTaskRepositoryAdapter`, `InMemoryTaskRepositoryAdapter` |

---

## 📁 Estructura del proyecto

```
src/main/java/com/kosmostecnologia/todohexagonal
│
├── domain/                                  ← 🟡 Núcleo: reglas de negocio puras
│   ├── model/
│   │   ├── Task.java                        Entidad con comportamiento (complete, reopen)
│   │   └── TaskStatus.java                  Enum: PENDING, COMPLETED
│   └── exception/
│       └── TaskNotFoundException.java
│
├── application/                             ← 🔵 Casos de uso
│   ├── port/
│   │   ├── in/                              Puertos de entrada (lo que se puede hacer)
│   │   │   ├── CreateTaskUseCase.java
│   │   │   ├── GetTaskUseCase.java
│   │   │   ├── ListTasksUseCase.java
│   │   │   ├── CompleteTaskUseCase.java
│   │   │   └── ReopenTaskUseCase.java
│   │   └── out/                             Puertos de salida (lo que se necesita)
│   │       └── TaskRepositoryPort.java
│   └── service/
│       └── TaskService.java                 Implementa todos los casos de uso
│
└── infrastructure/                          ← 🟢 Detalles técnicos
    └── adapter/
        ├── in/rest/                         Adaptador de entrada: API REST
        │   ├── TaskController.java
        │   ├── GlobalExceptionHandler.java
        │   └── dto/
        │       ├── CreateTaskRequest.java
        │       └── TaskResponse.java
        └── out/persistence/                 Adaptadores de salida: persistencia
            ├── InMemoryTaskRepositoryAdapter.java
            ├── JpaTaskRepositoryAdapter.java
            ├── SpringDataTaskRepository.java
            ├── TaskJpaEntity.java
            └── TaskPersistenceMapper.java
```

---

## 🔍 Las capas en detalle

### 🟡 Dominio

`Task` es un **modelo de dominio rico**: no es un simple contenedor de datos, sino que **protege sus propias reglas**.

```java
public void complete() {
    if (status == TaskStatus.COMPLETED) {
        throw new IllegalStateException("La tarea ya está completada");
    }
    status = TaskStatus.COMPLETED;
    completedAt = LocalDateTime.now();
}
```

- **No tiene setters**: el estado solo cambia mediante métodos con significado de negocio (`complete()`, `reopen()`), de modo que nunca puede quedar inconsistente (por ejemplo, `COMPLETED` sin `completedAt`).
- `@Builder` / `@AllArgsConstructor` se usan solo para **crear** o **reconstruir** la tarea (por ejemplo, desde la base de datos).
- No tiene ninguna anotación de Spring ni de JPA.

### 🔵 Aplicación

- **Puertos de entrada:** una interfaz por caso de uso (principio de segregación de interfaces). El controlador depende de estas interfaces, no de la implementación.
- **Puerto de salida:** `TaskRepositoryPort` define *qué* necesita la aplicación (`save`, `findAll`, `findById`, `update`), sin decir *cómo* se guarda.
- **`TaskService`:** orquesta el flujo — busca la tarea, delega la regla al dominio y persiste a través del puerto.

```java
public Task complete(Long id) {
    Task task = taskRepositoryPort.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    task.complete();                       // la regla vive en el dominio
    return taskRepositoryPort.update(task);
}
```

### 🟢 Infraestructura

**Adaptador de entrada (REST)**

- `TaskController` recibe HTTP, valida con `@Valid`, convierte DTO → dominio y llama al caso de uso.
- Los **DTOs** (`CreateTaskRequest`, `TaskResponse`) evitan exponer el modelo de dominio directamente en la API.
- `GlobalExceptionHandler` traduce excepciones a respuestas HTTP con `ProblemDetail` (RFC 9457).

**Adaptadores de salida (persistencia)**

| Clase | Rol |
|---|---|
| `TaskJpaEntity` | Entidad JPA (`@Entity`). **Separada** del dominio para que JPA no "contamine" `Task` |
| `TaskPersistenceMapper` | Convierte `Task` ⇄ `TaskJpaEntity` |
| `SpringDataTaskRepository` | Repositorio de Spring Data (`JpaRepository`) |
| `JpaTaskRepositoryAdapter` | Implementa `TaskRepositoryPort` usando JPA + H2 |
| `InMemoryTaskRepositoryAdapter` | Implementa `TaskRepositoryPort` con un `ConcurrentHashMap` |

---

## 🔄 Flujo de una petición

Ejemplo: `PATCH /api/v1/tasks/1/complete`

```
1. TaskController.completeTask(1)                  [infraestructura / in]
        │  depende de la interfaz CompleteTaskUseCase
        ▼
2. TaskService.complete(1)                         [aplicación]
        │  taskRepositoryPort.findById(1)
        ▼
3. JpaTaskRepositoryAdapter.findById(1)            [infraestructura / out]
        │  SpringData → TaskJpaEntity → mapper → Task
        ▼
4. task.complete()                                 [dominio]
        │  valida la regla y cambia el estado
        ▼
5. taskRepositoryPort.update(task)                 [aplicación → infraestructura]
        ▼
6. TaskResponse.from(task) → 200 OK                [infraestructura / in]
```

---

## 🔌 Intercambiar adaptadores (la gran ventaja)

Existen **dos implementaciones** de `TaskRepositoryPort`. Spring inyecta la que tenga `@Primary`:

```java
@Repository
@Primary   // ← mueve esta anotación para cambiar de adaptador
public class InMemoryTaskRepositoryAdapter implements TaskRepositoryPort { … }
```

Cambiar de almacenamiento en memoria a base de datos **no requiere tocar ni una línea** del dominio, del servicio ni del controlador. Esa es la promesa de la arquitectura hexagonal: **los detalles técnicos son reemplazables**.

---

## 🌐 Endpoints

Base URL: `http://localhost:8080/api/v1/tasks`

| Método | Ruta | Descripción | Respuesta exitosa |
|---|---|---|---|
| `POST` | `/` | Crear una tarea | `201 Created` |
| `GET` | `/` | Listar todas las tareas | `200 OK` |
| `GET` | `/{id}` | Obtener una tarea por ID | `200 OK` |
| `PATCH` | `/{id}/complete` | Marcar como completada | `200 OK` |
| `PATCH` | `/{id}/reopen` | Reabrir (volver a pendiente) | `200 OK` |

### Ejemplos

**Crear tarea**

```http
POST /api/v1/tasks
Content-Type: application/json

{
  "title": "Comprar pan",
  "description": "En la panadería"
}
```

```json
{
  "id": 1,
  "title": "Comprar pan",
  "description": "En la panadería",
  "status": "PENDING",
  "createdAt": "2026-11-11T14:50:44.661",
  "completedAt": null
}
```

**Completar tarea**

```http
PATCH /api/v1/tasks/1/complete
```

```json
{
  "id": 1,
  "title": "Comprar pan",
  "description": "En la panadería",
  "status": "COMPLETED",
  "createdAt": "2026-11-11T14:50:44.661",
  "completedAt": "2026-11-11T15:02:10.120"
}
```

> 💡 Se usa **`PATCH`** (y no `PUT`) para completar/reabrir porque se modifica **solo una parte** del recurso, y el cambio pasa siempre por los métodos del dominio.

---

## ⚠️ Manejo de errores

Todos los errores se devuelven en formato **`ProblemDetail`** (RFC 9457) desde `GlobalExceptionHandler`:

| Situación | Excepción | Código |
|---|---|---|
| La tarea no existe | `TaskNotFoundException` | `404 Not Found` |
| Falla una validación (`title` vacío) | `MethodArgumentNotValidException` | `400 Bad Request` |
| Cuerpo vacío o JSON mal formado | `HttpMessageNotReadableException` | `400 Bad Request` |
| Completar una tarea ya completada / reabrir una pendiente | `IllegalStateException` | `409 Conflict` |

**Ejemplo — validación (`POST` con `{}`):**

```json
{
  "detail": "Error de validación",
  "instance": "/api/v1/tasks",
  "status": 400,
  "title": "Datos inválidos",
  "errors": [
    "title: El titulo es obligatorio"
  ]
}
```

> 📝 **Lección aprendida:** si la petición se envía **sin cuerpo**, Spring no llega a validar: falla antes al leer el JSON (`HttpMessageNotReadableException`). Por eso se necesita un handler específico para ese caso; de lo contrario se obtiene la respuesta de error por defecto de Spring Boot.

---

## ▶️ Cómo ejecutar

**Requisitos:** JDK 25.

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (PowerShell / CMD)
mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Se puede probar con **Insomnia**, **Postman** o `curl`:

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
     -H "Content-Type: application/json" \
     -d '{"title":"Aprender arquitectura hexagonal"}'

curl http://localhost:8080/api/v1/tasks
```

> ℹ️ Con el adaptador en memoria (o con H2 en memoria), **los datos se pierden al reiniciar** la aplicación.

---

## 🎓 Conceptos aprendidos

Notas que surgieron durante el desarrollo del proyecto:

### Modelo de dominio sin setters
Un método privado de la clase **sí puede** modificar sus propios campos `private`; lo que impide `private` es el acceso **desde fuera**. Por eso `complete()` y `reopen()` funcionan sin setters. Si hiciera falta otro cambio, se crea un **método con nombre de negocio**, no un setter: el setter dice *"cambia este valor"*, el método de dominio dice *"qué pasó"* y garantiza un estado válido.

### `PUT` vs `PATCH`
| | `PUT` | `PATCH` |
|---|---|---|
| Qué se envía | El recurso **completo** | Solo los campos que cambian |
| Campos omitidos | Se reemplazan (quedan `null`) | Se conservan |

En ambos casos JPA usa `save()`: la diferencia no está en **cómo se guarda**, sino en **cómo se arma el objeto** antes de guardarlo (lógica que vive en el dominio/caso de uso, no en el controlador).

### `HashMap` vs `ConcurrentHashMap`
Los beans de Spring son **singleton** y cada petición HTTP corre en un hilo distinto, así que el repositorio en memoria es accedido concurrentemente. `ConcurrentHashMap` es seguro entre hilos; `HashMap` no. Para generar IDs se usa `AtomicLong` (`incrementAndGet()`), porque `contador++` no es atómico.

### Operador diamante `<>`
`new ConcurrentHashMap<>()` deduce los tipos del lado izquierdo (`Map<Long, Task>`). Hay que escribirlos explícitamente al usar `var`. Los paréntesis son los argumentos del constructor (capacidad inicial, copiar otro mapa, …); vacíos = valores por defecto.

### `List.of()` vs `new ArrayList<>()`
`List.of()` / `List.copyOf()` crean listas **inmutables** (ideal para devolver datos que no deben modificarse); `new ArrayList<>()` crea una lista **mutable**.

### Entidad de dominio ≠ entidad JPA
`Task` y `TaskJpaEntity` son clases distintas unidas por un *mapper*. Así el dominio no depende de JPA y la persistencia puede cambiar sin afectarlo.

---

## 🚀 Posibles mejoras

- [ ] Editar tareas (`PUT` / `PATCH /{id}`) y eliminarlas (`DELETE /{id}`).
- [ ] Usar *commands* propios en los puertos de entrada (p. ej. `CreateTaskCommand`) en lugar de recibir `Task` desde el controlador.
- [ ] Usar una excepción de dominio propia (p. ej. `InvalidTaskStateException`) en lugar de `IllegalStateException`.
- [ ] Pruebas unitarias del dominio y del servicio (con un *fake* del puerto de salida) y pruebas de integración del controlador.
- [ ] Documentar la API con OpenAPI / Swagger.
- [ ] Paginación y filtros por estado en el listado.

---

## 📄 Licencia

Proyecto con fines educativos.
