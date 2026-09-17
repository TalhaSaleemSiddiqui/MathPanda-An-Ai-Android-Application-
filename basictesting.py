import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from google import genai
import json
import os
from gtts import gTTS
import base64
import io


MY_API_KEY = "Enter_Your_Gemini_key"


client = genai.Client(api_key=MY_API_KEY)
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  
    allow_credentials=True,
    allow_methods=["*"],  
    allow_headers=["*"],  
)

class TutorRequest(BaseModel):
    message: str

def generate_audio_base64(text):
    try:
        tts = gTTS(text=text, lang='en', tld='co.uk') 
        mp3_fp = io.BytesIO()
        tts.write_to_fp(mp3_fp)
        mp3_fp.seek(0)
        b64_string = base64.b64encode(mp3_fp.read()).decode()
        return f"data:audio/mp3;base64,{b64_string}"
    except Exception as e:
        print(f"Audio Error: {e}")
        return None

@app.post("/tutor")
async def get_lesson(request: TutorRequest):
    print(f" User message: {request.message}")

    system_instruction = """
    You are MathPanda, an incredibly smart and fun AI Math Tutor.
    You have to solve each questions in stepwise.
    IMPORTANT: You MUST return the response STRICTLY as a JSON array of objects. 
    Each object must have exactly one key called "speech".
    Example: [{"speech": "Hello, let's solve this!"}, {"speech": "First, we add the numbers."}]
    """

    try:
        full_prompt = f"{system_instruction}\nUser asks: {request.message}"
        
        response = client.models.generate_content(
            model="gemini-2.5-flash", 
            contents=full_prompt
        )
        
        raw_text = response.text
        clean_text = raw_text.replace("```json", "").replace("```", "").strip()
        data = json.loads(clean_text)
        
        print("Generating Audio for steps...")
        for step in data:
            if "speech" in step:
                step["audio"] = generate_audio_base64(step["speech"])
        
        return data

    except Exception as e:
        print(f" Error: {e}")
        fallback_msg = "My math brain got frozen! Please ask again."
        return [
            { 
              "speech": fallback_msg, 
              "audio": generate_audio_base64(fallback_msg) 
            }
        ]

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8080)


   
