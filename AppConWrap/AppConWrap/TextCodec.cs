using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AppConWrap
{
    internal class TextCodec
    {
        private int Ptr;
        private List<char> Chrs;

        private byte[] ChrBuf;
        private int State;

        public TextCodec()
        {
            Ptr = 0;
            Chrs = new List<char>();
            State = 0;
            ChrBuf = new byte[100];
        }

        public bool HasText()
        {
            return (Ptr > 0) && (State == 0);
        }

        public string GetText()
        {
            if (Ptr == 0)
            {
                return null;
            }
            string Str = Encoding.UTF8.GetString(ChrBuf, 0, Ptr);
            Ptr = 0;
            return Str;
        }

        public bool PutByte(byte val)
        {
            ChrBuf[Ptr] = val;
            Ptr++;

            if (val < 128)
            {
                State = 0;
                return true;
            }
            else
            {
                if ((val & 0b11100000) == 0b11000000) { State = 1; return false; }
                if ((val & 0b11110000) == 0b11100000) { State = 2; return false; }
                if ((val & 0b11111000) == 0b11110000) { State = 3; return false; }
                if ((val & 0b11111100) == 0b11111000) { State = 4; return false; }
                if ((val & 0b11111110) == 0b11111100) { State = 5; return false; }

                if ((val & 0b11000000) == 0b10000000)
                {
                    State--;
                    return (State == 0);
                }
            }
            return false;
        }
    }
}
