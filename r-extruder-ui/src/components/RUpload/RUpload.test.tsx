import React from "react";
import { render } from "@testing-library/react";
import RUpload from "./RUpload";

jest.mock("axios");
describe("RUpload", () => {
  beforeAll(() => {
    // Mock window.URL.createObjectURL
    Object.defineProperty(window.URL, "createObjectURL", { value: jest.fn() });
  });

  it("renders correctly", () => {
    const { getByText, getByLabelText } = render(<RUpload />);

    expect(getByText("UPLOAD YOUR R FILES")).toBeInTheDocument();
    expect(
      getByLabelText("Select your returning parameter")
    ).toBeInTheDocument();
    expect(getByText("Submit")).toBeInTheDocument();
  });
});
