from app.schemas.ai import AiTestRequest, AiTestResponse


class AiService:
    """AI 작업을 처리하는 애플리케이션 서비스."""

    async def test(self, request: AiTestRequest) -> AiTestResponse:
        return AiTestResponse(
            status="SUCCESS",
            received_message=request.message,
            result=f"Python AI API received: {request.message}",
        )


def get_ai_service() -> AiService:
    """FastAPI 의존성 주입용 팩토리."""

    return AiService()