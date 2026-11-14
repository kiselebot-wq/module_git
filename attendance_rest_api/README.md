# Attendance REST API

REST API для автоматизированной системы учета посещаемости лабораторных занятий.

## Используемые технологии

- Python 3.12
- FastAPI
- Pydantic
- Swagger UI / OpenAPI
- Docker и Docker Compose
- GitHub Actions
- pytest и Ruff

## Запуск локально

```powershell
py -3.12 -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
uvicorn app.main:app --reload
```

Документация OpenAPI: http://127.0.0.1:8000/docs  
Альтернативная документация: http://127.0.0.1:8000/redoc

## Запуск через Docker Compose

```powershell
docker compose up -d --build
```

Остановка:

```powershell
docker compose down
```

После запуска API доступно по адресу http://localhost:8000.

## Методы API

| Метод | URL | Назначение | Успешный код |
|---|---|---|---|
| GET | `/api/v1/attendance/` | Получить все записи | 200 |
| GET | `/api/v1/attendance/{attendance_id}` | Получить запись по ID | 200 |
| POST | `/api/v1/attendance/` | Создать запись | 201 |
| DELETE | `/api/v1/attendance/{attendance_id}` | Удалить запись | 200 |

Ошибки: 400 — некорректные входные данные; 404 — запись не найдена.

## Пример тела POST-запроса

```json
{
  "student_name": "Иванов И.И.",
  "group_name": "ПО-41",
  "lesson_date": "2026-10-20",
  "lesson_name": "Корпоративные информационные системы",
  "attendance_status": "Присутствовал"
}
```

Допустимые значения `attendance_status`: `Присутствовал` и `Отсутствовал`.

## Проверка

```powershell
ruff check .
pytest -q
```

## CI

Workflow `.github/workflows/ci.yml` запускается при push в `main` и при создании pull request в `main`. Он устанавливает Python, устанавливает зависимости, выполняет Ruff, тесты и сборку Docker image.

## Ограничение учебной реализации

Данные хранятся в оперативной памяти процесса. Они не сохраняются после перезапуска приложения. Это соответствует варианту лабораторной работы, допускающему mock/in-memory хранилище.
