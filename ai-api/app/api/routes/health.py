from fastapi import APIRouter

router = APIRouter(tags=["Health"])


@router.get("/health")
async def health_check() -> dict[str, str]:
    """AI API 서버의 실행 상태를 확인한다."""

    return {
        "status": "UP",
        "service": "ai-api",
    }