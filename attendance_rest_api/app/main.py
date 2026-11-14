import logging
from datetime import date
from enum import Enum
from typing import Annotated

from fastapi import FastAPI, HTTPException, Path as ApiPath, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ConfigDict, Field

app = FastAPI(
    title="Attendance REST API",
    description=(
        "REST API для учета посещаемости лабораторных занятий. "
        "Сущность API — запись посещаемости студента."
    ),
    version="1.0.0",
)


class AttendanceStatus(str, Enum):
    PRESENT = "Присутствовал"
    ABSENT = "Отсутствовал"


class AttendanceCreate(BaseModel):
    """DTO для создания записи посещаемости."""

    student_name: str = Field(min_length=2, max_length=100, description="Фамилия и инициалы студента")
    group_name: str = Field(min_length=1, max_length=20, description="Учебная группа")
    lesson_date: date = Field(description="Дата лабораторного занятия")
    lesson_name: str = Field(min_length=1, max_length=150, description="Название лабораторного занятия")
    attendance_status: AttendanceStatus = Field(description="Результат посещения")


class Attendance(AttendanceCreate):
    """DTO записи посещаемости, возвращаемый клиенту."""

    model_config = ConfigDict(from_attributes=True)

    id: int = Field(description="Уникальный идентификатор записи")


class Message(BaseModel):
    message: str


# Учебное хранилище в оперативной памяти. После перезапуска приложения данные сбрасываются.
attendance_storage: dict[int, Attendance] = {}
next_id = 1


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    """Преобразует стандартный ответ FastAPI 422 в требуемый лабораторной 400."""
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"detail": exc.errors()},
    )


@app.get(
    "/api/v1/attendance/",
    response_model=list[Attendance],
    status_code=status.HTTP_200_OK,
    summary="Получить все записи посещаемости",
)
def get_attendance():
    return list(attendance_storage.values())


@app.get(
    "/api/v1/attendance/{attendance_id}",
    response_model=Attendance,
    status_code=status.HTTP_200_OK,
    summary="Получить запись по идентификатору",
)
def get_attendance_by_id(
    attendance_id: Annotated[int, ApiPath(gt=0, description="ID записи посещаемости")],
):
    record = attendance_storage.get(attendance_id)
    if record is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Запись посещаемости с ID {attendance_id} не найдена",
        )
    return record


@app.post(
    "/api/v1/attendance/",
    response_model=Attendance,
    status_code=status.HTTP_201_CREATED,
    summary="Создать запись посещаемости",
)
def create_attendance(data: AttendanceCreate):
    global next_id

    record = Attendance(id=next_id, **data.model_dump())
    attendance_storage[next_id] = record
    next_id += 1
    return record


@app.delete(
    "/api/v1/attendance/{attendance_id}",
    response_model=Message,
    status_code=status.HTTP_200_OK,
    summary="Удалить запись посещаемости",
)
def delete_attendance(
    attendance_id: Annotated[int, ApiPath(gt=0, description="ID записи посещаемости")],
):
    if attendance_id not in attendance_storage:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Запись посещаемости с ID {attendance_id} не найдена",
        )

    del attendance_storage[attendance_id]
    return {"message": f"Запись посещаемости с ID {attendance_id} удалена"}
@app.on_event("startup")
async def show_docs_url():
    logging.getLogger("uvicorn").info(
        "Swagger UI доступен по адресу: http://127.0.0.1:8000/docs"
    )
