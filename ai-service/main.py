from fastapi import FastAPI
from app.routes import router

app = FastAPI(
    title="AI Service",
    description="AI-powered recommendations, triage and search for distributed checkout platform",
    version="1.0.0"
)

app.include_router(router)

@app.get("/health")
def health():
    return {"status": "UP"}