package com.shankpai.model;

public class Complex {
	public double real;
	public double image;
	
	public Complex() {
		this.real = 0;
		this.image = 0;
	}

	public Complex(double real, double image){
		this.real = real;
		this.image = image;
	}
	
	public Complex(int real, int image) {
		Integer integer = real;
		this.real = integer.floatValue();
		integer = image;
		this.image = integer.floatValue();
	}
	
	public Complex(double real) {
		this.real = real;
		this.image = 0;
	}
	
	public Complex cc(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real * complex.real - this.image * complex.image;
		tmpComplex.image = this.real * complex.image + this.image * complex.real;
		return tmpComplex;
	}
	
	public Complex sum(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real + complex.real;
		tmpComplex.image = this.image + complex.image;
		return tmpComplex;
	}
	
	public Complex cut(Complex complex) {
		Complex tmpComplex = new Complex();
		tmpComplex.real = this.real - complex.real;
		tmpComplex.image = this.image - complex.image;
		return tmpComplex;
	}
	
	 // scalar multiplication
    // return a new object whose value is (this * alpha)
    public Complex times(double alpha) {
        return new Complex(alpha * real, alpha * image);
    }
	
 // return a new Complex object whose value is the conjugate of this
    public Complex conjugate() {  return new Complex(real, -image); }
    
	public int getIntValue(){
		int ret = 0;
//		System.out.println("real = " + real);
//		System.out.println("image = " + image);
		ret = (int) Math.round(Math.sqrt(this.real*this.real + this.image*this.image));
//		System.out.println("ret = " + ret);
		return ret;
	}
	
	public double getDoubleValue(){
		double ret = 0;
//		System.out.println("real = " + real);
//		System.out.println("image = " + image);
		ret = this.real*this.real + this.image*this.image;
//		System.out.println("ret = " + ret);
		return ret/1073741824.0;
	}
	
	public float getFloatValue(){
		float ret = 0;
//		System.out.println("real = " + real);
//		System.out.println("image = " + image);
		ret = (float) (this.real*this.real + this.image*this.image);
//		System.out.println("ret = " + ret);
		return (float) (ret/1073741824.0);
	}
}
