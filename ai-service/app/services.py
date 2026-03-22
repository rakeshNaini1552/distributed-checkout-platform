import os
import json
from dotenv import load_dotenv
from langchain_groq import ChatGroq
from langchain.prompts import ChatPromptTemplate
from langchain.schema.output_parser import StrOutputParser

load_dotenv()

llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    api_key=os.getenv("GROQ_API_KEY")
)

parser = StrOutputParser()

# ─── In-memory cache ─────────────────────────────────────
recommend_cache = {}

# ─── Recommend ───────────────────────────────────────────
recommend_prompt = ChatPromptTemplate.from_template("""
You are a helpful product recommendation engine for a tech store.

The user just purchased: {purchasedItem}
Category: {category}
Available products in our catalog: {availableProducts}

Suggest exactly 3 products from the available list that complement the purchase.
Return ONLY a JSON array of 3 product names. No explanation. No extra text.
Example: ["Mouse", "Keyboard", "Monitor"]
""")

recommend_chain = recommend_prompt | llm | parser

def get_recommendations(order_id: int, purchased_item: str, 
                         category: str, available_products: list) -> list:
    if order_id in recommend_cache:
        print(f"Cache hit for order {order_id}")
        return recommend_cache[order_id]

    result = recommend_chain.invoke({
        "purchasedItem": purchased_item,
        "category": category,
        "availableProducts": ", ".join(available_products)
    })

    try:
        recommendations = json.loads(result.strip())
    except json.JSONDecodeError:
        recommendations = [r.strip() for r in result.split(",")]

    recommend_cache[order_id] = recommendations
    return recommendations


# ─── Triage ──────────────────────────────────────────────
triage_prompt = ChatPromptTemplate.from_template("""
You are an incident triage assistant for an e-commerce platform.

Here are recent order failures:
{failures}

Analyze the failures and respond ONLY with a JSON object in this exact format:
{{
  "summary": "brief description of what is failing and why",
  "severity": <integer 1-5 where 5 is most severe>,
  "recommendedAction": "what the engineering team should do"
}}

No explanation. No extra text. Just the JSON.
""")

triage_chain = triage_prompt | llm | parser

def get_triage(failures: list) -> dict:
    failures_text = "\n".join([
        f"Order {f['orderId']}: item={f['item']}, reason={f['reason']}, time={f['timestamp']}"
        for f in failures
    ])

    result = triage_chain.invoke({"failures": failures_text})

    try:
        return json.loads(result.strip())
    except json.JSONDecodeError:
        return {
            "summary": result,
            "severity": 3,
            "recommendedAction": "Manual investigation required"
        }


# ─── Search ──────────────────────────────────────────────
search_prompt = ChatPromptTemplate.from_template("""
You are a search query parser for an e-commerce order history system.

User query: "{query}"

Extract search filters from this query and respond ONLY with a JSON object:
{{
  "item": <product name or null>,
  "status": <CREATED/CONFIRMED/REJECTED/COMPLETED/CANCELLED or null>,
  "dateFrom": <YYYY-MM-DD or null>,
  "dateTo": <YYYY-MM-DD or null>,
  "minPrice": <number or null>,
  "maxPrice": <number or null>
}}

No explanation. No extra text. Just the JSON.
""")

search_chain = search_prompt | llm | parser

def parse_search_query(query: str) -> dict:
    result = search_chain.invoke({"query": query})

    try:
        return json.loads(result.strip())
    except json.JSONDecodeError:
        return {
            "item": None,
            "status": None,
            "dateFrom": None,
            "dateTo": None,
            "minPrice": None,
            "maxPrice": None
        }