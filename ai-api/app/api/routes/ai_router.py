from typing import Annotated

from fastapi import APIRouter, Depends

from app.schemas.ai import AiTestRequest, AiTestResponse
from app.services.ai_service import AiService, get_ai_service

router = APIRouter(
    prefix="/api/v1/ai",
    tags=["AI"],
)

AiServiceDependency = Annotated[
    AiService,
    Depends(get_ai_service),
]


@router.post(
    "/test",
    response_model=AiTestResponse,
)
async def test_ai(
    request: AiTestRequest,
    ai_service: AiServiceDependency,
) -> AiTestResponse:
    return await ai_service.test(request)