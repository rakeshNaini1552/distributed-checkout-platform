from pydantic import BaseModel
from typing import List, Optional

# ─── Recommend ───────────────────────────────────────────
class RecommendRequest(BaseModel):
    orderId: int
    userId: int
    purchasedItem: str
    category: str
    availableProducts: List[str]

class RecommendResponse(BaseModel):
    orderId: int
    recommendations: List[str]

# ─── Triage ──────────────────────────────────────────────
class FailedOrder(BaseModel):
    orderId: str
    item: str
    reason: str
    timestamp: str

class TriageRequest(BaseModel):
    failures: List[FailedOrder]

class TriageResponse(BaseModel):
    summary: str
    severity: int
    recommendedAction: str

# ─── Search ──────────────────────────────────────────────
class SearchRequest(BaseModel):
    userId: int
    query: str

class SearchResponse(BaseModel):
    item: Optional[str] = None
    status: Optional[str] = None
    dateFrom: Optional[str] = None
    dateTo: Optional[str] = None
    minPrice: Optional[float] = None
    maxPrice: Optional[float] = None