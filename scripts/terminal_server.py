#!/usr/bin/env python3
"""Bank2Mp3 Bridge HTTP Server
监听 localhost:8899，接收 APK 发来的解码请求
"""
import json
import os
import subprocess
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler

SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPTS_DIR)

from decode import convert_bank

class BridgeHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        pass  # 静默日志
    
    def do_GET(self):
        if self.path == '/health':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok", "cwd": os.getcwd()}).encode())
        else:
            self.send_response(404)
            self.end_headers()
    
    def do_POST(self):
        length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(length).decode('utf-8')
        
        try:
            data = json.loads(body)
            cmd_type = data.get('cmd_type', 'exec')
            
            if cmd_type == 'exec':
                cmd = data['command']
                result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=120, cwd=SCRIPTS_DIR)
                resp = {"ok": result.returncode == 0, "stdout": result.stdout, "stderr": result.stderr}
            
            elif cmd_type == 'batch':
                bank_dir = data['bank_dir']
                output_dir = data.get('output_dir', bank_dir)
                fmt = data.get('format', 'wav')
                mp3_bitrate = data.get('mp3_bitrate', '192k')
                classify = data.get('classify', False)
                
                banks = [f for f in os.listdir(bank_dir) if f.endswith('.bank')]
                results = {}
                for bank in banks:
                    bank_path = os.path.join(bank_dir, bank)
                    bank_output = os.path.join(output_dir, os.path.splitext(bank)[0])
                    os.makedirs(bank_output, exist_ok=True)
                    files = convert_bank(bank_path, bank_output, fmt, mp3_bitrate)
                    results[bank] = {"files": files, "count": len(files)}
                
                resp = {"ok": True, "results": results, "total_banks": len(banks)}
                
                if classify:
                    from classify import classify_bank
                    resp['classification'] = classify_bank(results)
            
            elif cmd_type == 'wav2mp3':
                wav_dir = data['wav_dir']
                output_dir = data.get('output_dir', wav_dir + '_mp3')
                fmt = data.get('format', 'mp3')
                bitrate = data.get('mp3_bitrate', '192k')
                
                wavs = [f for f in os.listdir(wav_dir) if f.lower().endswith('.wav')]
                import shutil
                ffmpeg = shutil.which('ffmpeg') or '/usr/bin/ffmpeg'
                os.makedirs(output_dir, exist_ok=True)
                converted = []
                for wav in wavs:
                    in_path = os.path.join(wav_dir, wav)
                    base = os.path.splitext(wav)[0]
                    ext = {'mp3': 'mp3', 'flac': 'flac', 'aac': 'm4a', 'ogg': 'ogg', 'opus': 'opus'}.get(fmt, 'mp3')
                    out_path = os.path.join(output_dir, f"{base}.{ext}")
                    subprocess.run([ffmpeg, '-y', '-i', in_path, '-codec:a', 
                        'libmp3lame' if fmt == 'mp3' else fmt, '-b:a', bitrate, out_path],
                        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                    converted.append(out_path)
                resp = {"ok": True, "converted": converted, "count": len(converted)}
            
            else:
                resp = {"ok": False, "error": f"unknown cmd_type: {cmd_type}"}
            
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(resp).encode())
        
        except Exception as e:
            self.send_response(500)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"ok": False, "error": str(e)}).encode())

def main():
    port = 8899
    server = HTTPServer(('127.0.0.1', port), BridgeHandler)
    print(f"Bridge Server: http://127.0.0.1:{port} (Ctrl+C to stop)")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        server.shutdown()
        print("\nBridge stopped.")

if __name__ == '__main__':
    main()
