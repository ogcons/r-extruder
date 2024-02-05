import React from "react";
import { render } from "@testing-library/react";
import Rextruder from "./Rextruder";

test("should render Rextruder container", () => {
  const { getByText } = render(<Rextruder />);

  const uploadComponent = getByText("Upload your R files:");
  expect(uploadComponent).toBeInTheDocument();

  const singleSelectComponent = getByText("Drop files here to upload");
  expect(singleSelectComponent).toBeInTheDocument();
});
