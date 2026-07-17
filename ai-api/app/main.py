from fastapi import FastAPI

from app.api.routes import ai_router, health

app = FastAPI(
    title="KL Cube AI API",
    description="Platform API에서 호출하는 AI 작업 전용 API",
    version="0.1.0",
)

app.include_router(health.router)
app.include_router(ai_router.router)


@app.get("/")
async def root() -> dict[str, str]:
    return {
        "message": "KL Cube AI API",
        "status": "running",
    }