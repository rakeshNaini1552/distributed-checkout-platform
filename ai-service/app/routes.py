from fastapi import APIRouter, HTTPException
from app.models import (
    RecommendRequest, RecommendResponse,
    TriageRequest, TriageResponse,
    SearchRequest, SearchResponse
)
from app import services

router = APIRouter(prefix="/ai", tags=["AI"])


@router.post("/recommend", response_model=RecommendResponse)
async def recommend(request: RecommendRequest):
    try:
        recommendations = services.get_recommendations(
            order_id=request.orderId,
            purchased_item=request.purchasedItem,
            category=request.category,
            available_products=request.availableProducts
        )
        return RecommendResponse(
            orderId=request.orderId,
            recommendations=recommendations
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/triage", response_model=TriageResponse)
async def triage(request: TriageRequest):
    try:
        failures = [f.dict() for f in request.failures]
        result = services.get_triage(failures)
        return TriageResponse(
            summary=result.get("summary", "Unable to analyze"),
            severity=result.get("severity", 3),
            recommendedAction=result.get("recommendedAction", "Manual investigation required")
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/search", response_model=SearchResponse)
async def search(request: SearchRequest):
    try:
        result = services.parse_search_query(request.query)
        return SearchResponse(
            item=result.get("item"),
            status=result.get("status"),
            dateFrom=result.get("dateFrom"),
            dateTo=result.get("dateTo"),
            minPrice=result.get("minPrice"),
            maxPrice=result.get("maxPrice")
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))