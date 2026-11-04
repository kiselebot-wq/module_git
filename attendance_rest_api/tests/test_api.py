from fastapi.testclient import TestClient

from app.main import app, attendance_storage

client = TestClient(app)


def setup_function():
    attendance_storage.clear()


def sample_record():
    return {
        "student_name": "Иванов И.И.",
        "group_name": "ПО-41",
        "lesson_date": "2026-10-20",
        "lesson_name": "Корпоративные информационные системы",
        "attendance_status": "Присутствовал",
    }


def test_create_and_get_attendance():
    response = client.post("/api/v1/attendance/", json=sample_record())
    assert response.status_code == 201
    created = response.json()
    assert created["id"] == 1

    response = client.get("/api/v1/attendance/1")
    assert response.status_code == 200
    assert response.json()["student_name"] == "Иванов И.И."


def test_get_all_attendance():
    client.post("/api/v1/attendance/", json=sample_record())
    response = client.get("/api/v1/attendance/")
    assert response.status_code == 200
    assert len(response.json()) == 1


def test_delete_attendance():
    client.post("/api/v1/attendance/", json=sample_record())
    response = client.delete("/api/v1/attendance/1")
    assert response.status_code == 200
    assert client.get("/api/v1/attendance/1").status_code == 404


def test_not_found():
    assert client.get("/api/v1/attendance/999").status_code == 404


def test_invalid_data_returns_400():
    data = sample_record()
    data["student_name"] = ""
    response = client.post("/api/v1/attendance/", json=data)
    assert response.status_code == 400
