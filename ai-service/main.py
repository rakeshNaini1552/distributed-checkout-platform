from fastapi import FastAPI
from app.routes import router
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(
    title="AI Service",
    description="AI-powered recommendations, triage and search for distributed checkout platform",
    version="1.0.0"
)

app.include_router(router)

@app.get("/health")
def health():
    return {"status": "UP"}

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("AI_SERVICE_PORT", 8085))
    uvicorn.run(app, host="0.0.0.0", port=port)