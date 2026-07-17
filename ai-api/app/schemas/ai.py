from pydantic import BaseModel, Field


class AiTestRequest(BaseModel):
    """Java platform-api에서 전달받는 테스트 요청."""

    message: str = Field(
        min_length=1,
        max_length=500,
        description="Java에서 전달하는 테스트 메시지",
    )


class AiTestResponse(BaseModel):
    """Python AI API가 Java로 반환하는 테스트 응답."""

    status: str
    received_message: str
    result: str