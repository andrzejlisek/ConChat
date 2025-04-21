using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace AppConWrap
{
    internal class Program
    {

        static TextCodec TextCodec_ = new TextCodec();

        static Process App = null;
        static Stream StrI;
        static Stream StrO;
        static Stream StrE;
        static byte[] StreamBufO = new byte[1000000];
        static byte[] StreamBufE = new byte[1000000];

        static int ScreenInsideCommand = -1;
        static object mtx = new object();


        static void ScreenWrtite(byte b)
        {
            if ((b == 27) || (b == 10))
            {
                ScreenFlush();
            }
            TextCodec_.PutByte(b);
            if (TextCodec_.HasText())
            {
                Console.Write(TextCodec_.GetText());
            }
        }

        static void ScreenFlush()
        {
        }

        static void AppOutText(byte[] Buf, int Offset, int BufL)
        {
            for (int i = Offset; i < (BufL + Offset); i++)
            {
                if (ScreenInsideCommand >= 0)
                {
                    bool ScreenCommandEnd = false;
                    int t = i - ScreenInsideCommand;

                    switch ((char)Buf[i - t + 1])
                    {
                        case '[':
                            if ((Buf[i] >= 65) && (Buf[i] <= 90)) { ScreenCommandEnd = true; }
                            if ((Buf[i] >= 97) && (Buf[i] <= 122)) { ScreenCommandEnd = true; }
                            switch (Buf[i])
                            {
                                case 0x40: // @
                                case 0x60: // `
                                case 0x7B: // {
                                case 0x7D: // }
                                case 0x7E: // ~
                                case 0x7C: // |
                                    ScreenCommandEnd = true;
                                    break;
                            }
                            break;
                        default:
                            if (t == 1)
                            {
                                ScreenCommandEnd = true;
                            }
                            break;
                    }

                    if (ScreenCommandEnd)
                    {
                        ScreenInsideCommand = -1;

                        bool CommandStd = true;

                        if (Buf[i - t + 1] == (byte)'[')
                        {
                            string[] CmdPar = Encoding.UTF8.GetString(Buf, i - t + 2, t - 2).Split(';');
                            int[] CmdParI = new int[CmdPar.Length];
                            for (int ii = 0; ii < CmdPar.Length; ii++)
                            {
                                try
                                {
                                    CmdParI[ii] = int.Parse(CmdPar[ii]);
                                }
                                catch
                                {
                                    CmdParI[ii] = 1;
                                }
                            }

                            switch (Buf[i])
                            {
                                case (byte)'n':
                                    if ((CmdParI[0] == 6) && (t == 3))
                                    {
                                        Send(27);
                                        Send("[");
                                        Send((Console.CursorTop + 1).ToString());
                                        Send(";");
                                        Send((Console.CursorLeft + 1).ToString());
                                        Send("R");
                                    }
                                    CommandStd = false;
                                    break;
                            }
                        }

                        if (CommandStd)
                        {
                            ScreenWrtite(27);
                            AppOutText(Buf, i - t + 1, t);
                        }
                    }
                }
                else
                {
                    if (Buf[i] >= 32)
                    {
                        ScreenWrtite(Buf[i]);
                    }
                    switch (Buf[i])
                    {
                        case 7:
                            Console.Write("\a");
                            break;
                        case 10:
                            ScreenWrtite(10);
                            break;
                        case 27:
                            ScreenInsideCommand = i;
                            break;
                    }
                }
            }
        }

        static void LoopOut()
        {
            while (true)
            {
                try
                {
                    int Avail = StrO.Read(StreamBufO, 0, StreamBufO.Length);
                    if (Avail > 0)
                    {
                        Monitor.Enter(mtx);
                        AppOutText(StreamBufO, 0, Avail);
                        ScreenFlush();
                        Monitor.Exit(mtx);
                    }
                    else
                    {
                        if (Status() == 0)
                        {
                            break;
                        }
                    }
                }
                catch
                {
                    break;
                }
            }
            try
            {
                StrO.Close();
            }
            catch (Exception E)
            {

            }
        }

        static void LoopErr()
        {
            while (true)
            {
                try
                {
                    int Avail = StrE.Read(StreamBufE, 0, StreamBufE.Length);
                    if (Avail > 0)
                    {
                        Monitor.Enter(mtx);
                        AppOutText(StreamBufE, 0, Avail);
                        ScreenFlush();
                        Monitor.Exit(mtx);
                    }
                    else
                    {
                        if (Status() == 0)
                        {
                            break;
                        }
                    }
                }
                catch
                {
                    break;
                }
            }
            try
            {
                StrE.Close();
            }
            catch (Exception E)
            {

            }
        }

        static Thread LoopOutThr;
        static Thread LoopErrThr;

        static void Open(string AppFileName, string AppArguments)
        {
            App = new Process();
            App.StartInfo.FileName = AppFileName;
            App.StartInfo.Arguments = AppArguments;
            App.StartInfo.RedirectStandardInput = true;
            App.StartInfo.RedirectStandardOutput = true;
            App.StartInfo.RedirectStandardError = true;

            App.StartInfo.CreateNoWindow = true;
            App.StartInfo.UseShellExecute = false;
            App.EnableRaisingEvents = false;
            App.StartInfo.StandardOutputEncoding = Encoding.UTF8;
            App.StartInfo.StandardErrorEncoding = Encoding.UTF8;
            if (App.Start())
            {
                StrI = App.StandardInput.BaseStream;
                StrO = App.StandardOutput.BaseStream;
                StrE = App.StandardError.BaseStream;

                LoopOutThr = new Thread(LoopOut);
                LoopOutThr.Start();
                LoopErrThr = new Thread(LoopErr);
                LoopErrThr.Start();
            }
            else
            {
                App = null;
            }
        }

        static int Status()
        {
            if (App == null)
            {
                return 0;
            }
            if (App.HasExited)
            {
                return 0;
            }
            return 1;
        }

        static void Close()
        {
            try
            {
                App.Kill();
            }
            catch (Exception E)
            {
            }
            try
            {
                StrI.Close();
            }
            catch (Exception E)
            {

            }
            App = null;
        }

        static void Send(byte Data)
        {
            //App.StandardInput.Write(Data);
            //App.StandardInput.Flush();
            StrI.WriteByte(Data);
            StrI.Flush();
        }

        static void Send(string Data)
        {
            //App.StandardInput.Write(Data);
            //App.StandardInput.Flush();
            byte[] Data_ = Encoding.UTF8.GetBytes(Data);
            StrI.Write(Data_, 0, Data_.Length);
            StrI.Flush();
        }

        static void Send(char Data)
        {
            Send(Data.ToString());
        }

        static void Main(string[] args)
        {
            if (args.Length == 0)
            {
                Console.WriteLine("Usage: AppConWrap.exe Command [Parameter1] [Parameter2] ...");
                return;
            }

            //Console.InputEncoding = Encoding.UTF8;
            //Console.OutputEncoding = Encoding.UTF8;
            Console.Clear();
            string AppFile = args[0];
            string AppParams = "";
            for (int i = 1; i < args.Length; i++)
            {
                if (i > 1) AppParams = AppParams + " ";
                AppParams = AppParams + args[i];
            }
            Open(AppFile, AppParams);

            while (Status() > 0)
            {
                ConsoleKeyInfo CKI = Console.ReadKey(true);
                switch (CKI.Key)
                {
                    case ConsoleKey.Enter: Send('\n'); break;
                    case ConsoleKey.UpArrow: Send(27); Send("[A"); break;
                    case ConsoleKey.DownArrow: Send(27); Send("[B"); break;
                    case ConsoleKey.RightArrow: Send(27); Send("[C"); break;
                    case ConsoleKey.LeftArrow: Send(27); Send("[D"); break;

                    case ConsoleKey.Insert: Send(27); Send("[2~"); break;
                    case ConsoleKey.Delete: Send(27); Send("[3~"); break;
                    case ConsoleKey.Home: Send(27); Send("[H"); break;
                    case ConsoleKey.End: Send(27); Send("[F"); break;
                    case ConsoleKey.PageUp: Send(27); Send("[5~"); break;
                    case ConsoleKey.PageDown: Send(27); Send("[6~"); break;

                    case ConsoleKey.F1: Send(27); Send("OP"); break;
                    case ConsoleKey.F2: Send(27); Send("OQ"); break;
                    case ConsoleKey.F3: Send(27); Send("OR"); break;
                    case ConsoleKey.F4: Send(27); Send("OS"); break;
                    case ConsoleKey.F5: Send(27); Send("OT"); break;
                    case ConsoleKey.F6: Send(27); Send("OU"); break;
                    case ConsoleKey.F7: Send(27); Send("OV"); break;
                    case ConsoleKey.F8: Send(27); Send("OW"); break;
                    case ConsoleKey.F9: Send(27); Send("OX"); break;
                    case ConsoleKey.F10: Send(27); Send("YP"); break;
                    case ConsoleKey.F11: Send(27); Send("OZ"); break;
                    case ConsoleKey.F12: Send(27); Send("O["); break;

                    case ConsoleKey.Backspace: Send(127); break;

                    default:
                        Send(CKI.KeyChar);
                        break;
                }
            }
            Close();
        }
    }
}
